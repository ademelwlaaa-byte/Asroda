# 🚨 SIFIR KALİTE KAYBIYLA GÖRSEL OPTİMİZASYONU VE HAFİF BOT AVATAR MİMARİSİ ŞARTNAMESİ

> **⚠️ YAPAY ZEKA GELİŞTİRİCİSİ İÇİN KESİN MÜHENDİSLİK TALİMATI (ZERO-LAZINESS & PRODUCTION-GRADE):**
> Uygulamadaki menü tasarım görsellerinin depolama ve bellek (RAM) boyutunu gözle görülür hiçbir kalite kaybı olmadan **%75–%85 oranında küçültecek**, ayrıca kullanıcı yeni bot oluştururken seçtiği avatarların depolamada neredeyse **sıfır yer kaplamasını** ve veritabanını (Room/SQLite/JSON) şişirmemesini sağlayacak uçtan uca optimizasyon sistemini kodlayacaksın.
> Kısaltma (`// ...`) yapmak, sahte fonksiyon bırakmak veya bellek sızıntısına (OOM) yol açacak ham Bitmap işlemleri yazmak **KESİNLİKLE YASAKTIR**!

---

## 🎯 BÖLÜM 1: BOT OLUŞTURURKEN EKLENEN AVATARLAR İÇİN "MICRO-WEBP + HAFİF BASE64" PIPELINE'I

### 🔍 Neden Doğrudan Ham Base64 Kullanmıyoruz ve Nasıl Çözüyoruz?
* Kullanıcının galeriden seçtiği 4–6 MB'lık ham fotoğrafı doğrudan Base64'e çevirmek veriyi **%33 daha da büyütür (~7 MB metin)** ve veritabanı sorgularında `CursorWindow` çökmesine veya ciddi yavaşlamaya yol açar.
* **Kesin Çözüm (İki Aşamalı Sıkıştırma):**
  1. Kullanıcı fotoğrafı seçtiği anda arka planda (`Dispatchers.IO`), görsel belleğe tam boyutta yüklenmeden önce (`inJustDecodeBounds` + `inSampleSize` ile OOM korumalı olarak) okunur.
  2. Görsel merkezden kare (`1:1 CenterCrop`) olarak **`256 x 256 px`** boyutuna ölçeklenir ve **`Bitmap.CompressFormat.WEBP_LOSSY` (`quality = 82`)** ile sıkıştırılır.
  3. Bu işlem sonucunda 5 MB'lık fotoğraf **yalnızca ~12–18 KB** boyutuna iner ve insan gözüyle cam gibi keskin görünür.
  4. **Hibrit Saklama:** Sıkıştırılmış bu `~15 KB` boyutundaki Micro-WebP byte dizisi, istenirse doğrudan hafif Base64 (`data:image/webp;base64,...`, toplam ~20 KB) olarak bot nesnesinin içinde saklanabilir veya uygulama içi önbellekte tutularak hem dışa aktarımda (JSON yedekleme) taşınabilir hem de cihazda neredeyse hiç yer kaplamaz!

### 🛠️ Zorunlu Kotlin / Jetpack Compose Yardımcı Sınıfı (`AvatarOptimizer.kt`)
Bu sınıfı hiçbir satırını atlamadan projeye ekle ve **Bot Oluşturma / Düzenleme** ekranındaki görsel seçiciye (`ActivityResultContracts.GetContent` / `PickVisualMedia`) doğrudan bağla:

```kotlin
package com.emochi.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.min

object AvatarOptimizer {

    private const val TARGET_AVATAR_SIZE = 256 // 256x256 px: Avatarlar için kristal netlik + minimum boyut
    private const val WEBP_QUALITY = 82        // Görsel kalite kaybı olmadan maksimum sıkıştırma

    /**
     * Galeriden seçilen yüksek boyutlu fotoğrafı OOM (OutOfMemory) hatası vermeden okur,
     * EXIF yönünü düzeltir, 256x256 kare kırpar, WebP (%82) sıkıştırır ve ~15 KB'lık
     * hafif Base64 Data URI string olarak döndürür.
     */
    suspend fun processAvatarToMicroWebPBase64(
        context: Context,
        imageUri: Uri
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Aşama: Görseli belleğe yüklemeden sadece boyutlarını oku
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }

            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) {
                return@withContext Result.failure(IllegalArgumentException("Geçersiz görsel dosyası"))
            }

            // 2. Aşama: Belleği korumak için inSampleSize hesapla
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = calculateInSampleSize(
                    boundsOptions.outWidth,
                    boundsOptions.outHeight,
                    TARGET_AVATAR_SIZE * 2,
                    TARGET_AVATAR_SIZE * 2
                )
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val sampledBitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Görsel çözümlenemedi"))

            // 3. Aşama: Kamera/Galeri EXIF döndürme açısını düzelt
            val rotatedBitmap = fixExifOrientation(context, imageUri, sampledBitmap)
            if (rotatedBitmap !== sampledBitmap) {
                sampledBitmap.recycle()
            }

            // 4. Aşama: Merkezden 1:1 kare kırp ve 256x256 boyutuna ölçekle
            val croppedSquare = centerCropSquare(rotatedBitmap)
            if (croppedSquare !== rotatedBitmap) {
                rotatedBitmap.recycle()
            }

            val finalBitmap = Bitmap.createScaledBitmap(
                croppedSquare,
                TARGET_AVATAR_SIZE,
                TARGET_AVATAR_SIZE,
                true
            )
            if (finalBitmap !== croppedSquare) {
                croppedSquare.recycle()
            }

            // 5. Aşama: WebP formatında sıkıştır (~12-18 KB)
            val outputStream = ByteArrayOutputStream()
            val compressFormat = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }

            finalBitmap.compress(compressFormat, WEBP_QUALITY, outputStream)
            finalBitmap.recycle()

            val webpBytes = outputStream.toByteArray()
            val base64String = Base64.encodeToString(webpBytes, Base64.NO_WRAP)

            Result.success("data:image/webp;base64,$base64String")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateInSampleSize(
        rawWidth: Int,
        rawHeight: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var inSampleSize = 1
        if (rawHeight > reqHeight || rawWidth > reqWidth) {
            val halfHeight = rawHeight / 2
            val halfWidth = rawWidth / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun centerCropSquare(source: Bitmap): Bitmap {
        val dimension = min(source.width, source.height)
        val xOffset = (source.width - dimension) / 2
        val yOffset = (source.height - dimension) / 2
        return Bitmap.createBitmap(source, xOffset, yOffset, dimension, dimension)
    }

    private fun fixExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL

            val degrees = when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }

            if (degrees == 0f) {
                bitmap
            } else {
                val matrix = Matrix().apply { postRotate(degrees) }
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            }
        } catch (_: Exception) {
            bitmap
        }
    }
}
```

### 🛡️ Bot Silindiğinde ve Yedeklendiğinde Temizlik Garantisi:
* Bot verisi içinde `data:image/webp;base64,...` formatında saklanan bu görsel ortalama **15–20 KB** olduğu için:
  1. Cihazda ekstra klasör kalıntısı veya sahipsiz dosya bırakmaz.
  2. Bot silindiğinde veritabanı satırıyla birlikte anında bellekten ve depolamadan silinir.
  3. Coil `AsyncImage` bileşeni `data:image/webp;base64,...` URI'lerini doğrudan ve önbellekli olarak sıfır gecikmeyle çizer.

---

## 🖼️ BÖLÜM 2: UYGULAMA MENÜ TASARIM GÖRSELLERİNDE SIFIR KALİTE KAYBIYLA %80 BOYUT TASARRUFU

Uygulamanın kendi içindeki menü arka planları, hikaye kapakları ve evren görsellerinin APK/AAB boyutunu ve RAM kullanımını şişirmesini önlemek için şu **4 mühendislik kuralını** eksiksiz uygula:

### 1. Coil Akıllı Önbellek ve Donanım Bitmap Yapılandırması (`ImageLoader` Konfigürasyonu)
* Uygulama genelindeki Coil `ImageLoader` yapılandırmasını optimize et:
  * **Bellek Önbelleği (Memory Cache):** Cihazın kullanılabilir RAM'inin en fazla `%18`'ini kullanacak şekilde sınırla (`MemoryCache.Builder(context).maxSizePercent(0.18).build()`).
  * **Disk Önbelleği (Disk Cache):** `35 MB` ile sınırlandır (`DiskCache.Builder().directory(context.cacheDir.resolve("image_cache")).maxSizeBytes(35L * 1024 * 1024).build()`).
  * **Crossfade Geçişi:** `crossfade(300)` varsayılan olsun.
  * **Tam Boyut Yükleme Yasağı:** Hiçbir `AsyncImage` çağrısında `Size.ORIGINAL` kullanma! Coil'in hedef Compose kutusunun (`Modifier.size(...)` veya `fillMaxWidth()`) piksel boyutuna göre otomatik **Downsampling** yapmasını sağla.

### 2. Katman Bazlı Çözünürlük ve Renk Formatı Standardı
* **Tam Ekran Menü Arka Planları (Katedral, Mor Salkımlar, Nebula vb.):**
  * Şeffaflık (alpha kanalı) gerektirmeyen tam ekran arka plan görsellerinde `Bitmap.Config.RGB_565` kullan (`allowRgb565(true)`). Bu sayede görsel kalitesi korunurken **RAM tüketimi doğrudan %50 azalır**!
  * Arka plan görselleri en fazla `1080 x 2400 px` çözünürlükte ve **WebP (%85 kalite)** formatında tutulmalıdır.
* **Hikaye ve Evren Kartı Kapak Görselleri:**
  * Kart içinde küçük/orta boyutta görünen görseller için `512 x 512 px` (veya dikdörtgen kartlar için `640 x 360 px`) yeterlidir. `2048px` veya `4K` kaynak görselleri bu boyutlara optimize ederek dosya başına boyutunu **2 MB'tan ~85 KB'a** düşür.

### 3. Görsel Dosyası Yerine Jetpack Compose Vektör & Shader Çizimi (0 KB Yer Kaplama)
* Aşağıdaki tasarım öğelerini kesinlikle PNG/JPG dosyası olarak projeye ekleme; tamamını **Jetpack Compose `Canvas`, `Brush` ve `graphicsLayer`** ile çizdir:
  * Mesaj balonlarının ve butonların altındaki yatay ışık yansımaları (`Brush.horizontalGradient`)
  * Bot mesaj balonlarının üst çerçevesindeki 4 köşeli keskin yıldız (`✦` - Compose `Path` çizimi)
  * Sağ üstteki 3D oynatma kartı etrafındaki dönen yörünge halkaları ve parlamalar
  * Arka plandaki reaktif nebula ışık küreleri (`Brush.radialGradient`) ve yükselen kozmik kıvılcımlar

### 4. Gradle Build Optimizasyonu (`build.gradle.kts`)
* Uygulama derlenirken kullanılmayan kaynakların ve gereksiz yoğunluk (density) kopyalarının APK'ya girmesini önlemek için `android` bloğunda:
  * `isMinifyEnabled = true` ve `isShrinkResources = true` (release build için) yapılandırmasını aktif tut.
  * Tekrarlanan veya kullanılmayan `drawable-xxxhdpi` / `drawable-xxhdpi` PNG kopyaları yerine yalnızca `drawable-nodpi` içinde optimize edilmiş `.webp` varlıklarını kullan (böylece Android sistemi aynı görseli farklı ekran yoğunlukları için yeniden ölçekleyip RAM şişirmesi yapmaz).

---

## ✅ TESLİMAT ÖNCESİ ZORUNLU KONTROL LİSTESİ

1. **[✓] `AvatarOptimizer.kt` Entegre Edildi mi?** Bot oluşturma ve düzenleme ekranında seçilen görsel `256x256 px` WebP (%82 kalite) formatına sıkıştırılıp `~15 KB` boyutunda `data:image/webp;base64,...` olarak kaydediliyor mu?
2. **[✓] OOM (OutOfMemory) Koruması Var mı?** Yüksek çözünürlüklü bir kamera fotoğrafı (10+ MB) seçildiğinde uygulama çökmeden `inSampleSize` ile güvenli şekilde işleniyor mu?
3. **[✓] EXIF Yön Düzeltmesi Çalışıyor mu?** Galeriden seçilen dikey fotoğraflar yan dönmeden doğru açıyla kaydediliyor mu?
4. **[✓] Coil Önbellek ve `allowRgb565` Ayarlandı mı?** Menü arka planları ve kart görselleri `Size.ORIGINAL` kullanmadan hedef boyuta göre ölçeklenip RAM tasarrufu sağlıyor mu?
5. **[✓] Efektler 0 KB Compose Çizimi mi?** Yıldızlar, yörünge halkaları, ışık yansımaları ve nebula arka planları görsel dosyası yerine Compose koduyla çiziliyor mu?

**Şimdi hiçbir detayı atlamadan ve kısaltma yapmadan bu optimizasyon mimarisini projeye entegre et!**