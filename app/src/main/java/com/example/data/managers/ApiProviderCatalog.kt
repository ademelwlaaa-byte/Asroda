package com.example.data.managers

import com.example.data.local.CustomProviderEntity
import com.example.data.local.UserSettingsEntity
import java.util.Locale

enum class ApiCatalogCategory(val label: String) {
    ALL("Tümü"),
    POPULAR("Popüler"),
    FREE_TIER("Ücretsiz Kota"),
    FAST("Hızlı Çıkarım"),
    AGGREGATOR("Çoklu Model"),
    LOCAL("Yerel")
}

data class CatalogProviderSpec(
    val id: String,
    val name: String,
    val shortDesc: String,
    val badge: String,
    val categories: List<ApiCatalogCategory>,
    val baseUrl: String,
    val apiFormat: String, // "gemini" | "openai" | "anthropic"
    val builtinSlot: String? = null, // "gemini", "openai", "claude", "groq", "openrouter", "nvidia", "mistral"
    val defaultModel: String,
    val popularModels: List<String>,
    val keyHint: String,
    val requiresApiKey: Boolean = true
)

data class FreeKeylessProviderSpec(
    val id: String,
    val name: String,
    val badge: String,
    val description: String,
    val endpointHint: String,
    val models: List<String>,
    val defaultModel: String
)

object ApiProviderCatalog {

    val BUILTIN_KEYED_SLOTS = listOf(
        "gemini", "openai", "claude", "groq", "openrouter", "nvidia", "mistral"
    )

    val FREE_KEYLESS_IDS = listOf("pollinations", "llm7", "ovh")

    val freeKeylessProviders: List<FreeKeylessProviderSpec> = listOf(
        FreeKeylessProviderSpec(
            id = "pollinations",
            name = "Pollinations AI",
            badge = "Anahtarsız • Sınırsız",
            description = "API anahtarı veya hesap gerektirmeyen topluluk destekli ücretsiz metin motoru. OpenAI, Mistral, Qwen ve Llama tabanlı hafif modellerle hızlı sohbet yanıtları üretir.",
            endpointHint = "text.pollinations.ai",
            models = listOf("openai", "mistral", "qwen", "llama"),
            defaultModel = "openai"
        ),
        FreeKeylessProviderSpec(
            id = "llm7",
            name = "LLM7 (api.llm7.io)",
            badge = "Anahtarsız • OpenAI Uyumlu",
            description = "Üyelik ve API anahtarı istemeyen ücretsiz OpenAI uyumlu ağ geçidi. Dakika ve günlük token kotası dahilinde kararlı bir birincil veya yedek sağlayıcı olarak çalışır.",
            endpointHint = "api.llm7.io/v1",
            models = listOf("default"),
            defaultModel = "default"
        ),
        FreeKeylessProviderSpec(
            id = "ovh",
            name = "OVHcloud AI Endpoints",
            badge = "Anahtarsız • Avrupa Sunucu",
            description = "OVHcloud'un anonim ücretsiz katmanı üzerinden hesap veya anahtar olmadan açık kaynaklı büyük modelleri çalıştırır. IP başına dakikada 2 istek sınırı bulunduğundan özellikle yedek veya sakin sohbetler için idealdir.",
            endpointHint = "oai.endpoints.kepler.ai.cloud.ovh.net/v1",
            models = listOf(
                "Meta-Llama-3_3-70B-Instruct",
                "Mistral-Small-3.1-24B-Instruct-2503",
                "Qwen2.5-Coder-32B-Instruct",
                "meta-llama/Meta-Llama-3-70B-Instruct"
            ),
            defaultModel = "Meta-Llama-3_3-70B-Instruct"
        )
    )

    val catalogProviders: List<CatalogProviderSpec> = listOf(
        CatalogProviderSpec(
            id = "gemini",
            name = "Google Gemini",
            shortDesc = "Google AI Studio üzerinden geniş bağlamlı ve hızlı Gemini modelleri.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.POPULAR, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://generativelanguage.googleapis.com/v1beta",
            apiFormat = "gemini",
            builtinSlot = "gemini",
            defaultModel = "gemini-2.0-flash",
            popularModels = listOf(
                "gemini-2.5-flash",
                "gemini-2.5-pro",
                "gemini-2.0-flash",
                "gemini-2.0-flash-lite",
                "gemini-1.5-pro"
            ),
            keyHint = "aistudio.google.com/apikey"
        ),
        CatalogProviderSpec(
            id = "openai",
            name = "OpenAI (ChatGPT)",
            shortDesc = "GPT-4.1, GPT-4o ve o3 serisi yüksek tutarlılıklı sohbet modelleri.",
            badge = "Popüler",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.openai.com/v1",
            apiFormat = "openai",
            builtinSlot = "openai",
            defaultModel = "gpt-4o",
            popularModels = listOf(
                "gpt-4.1",
                "gpt-4.1-mini",
                "gpt-4o",
                "gpt-4o-mini",
                "o3-mini"
            ),
            keyHint = "platform.openai.com/api-keys"
        ),
        CatalogProviderSpec(
            id = "claude",
            name = "Anthropic Claude",
            shortDesc = "Edebi derinlik ve uzun rol yapma kurgularında üst düzey karakter tutarlılığı.",
            badge = "Edebi RP",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.anthropic.com",
            apiFormat = "anthropic",
            builtinSlot = "claude",
            defaultModel = "claude-3-5-sonnet-20241022",
            popularModels = listOf(
                "claude-sonnet-4-20250514",
                "claude-3-7-sonnet-20250219",
                "claude-3-5-sonnet-20241022",
                "claude-3-5-haiku-20241022"
            ),
            keyHint = "console.anthropic.com/settings/keys"
        ),
        CatalogProviderSpec(
            id = "openrouter",
            name = "OpenRouter",
            shortDesc = "Tek API anahtarıyla 300+ ücretli ve :free etiketli ücretsiz modele erişim.",
            badge = "300+ Model",
            categories = listOf(ApiCatalogCategory.POPULAR, ApiCatalogCategory.AGGREGATOR, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://openrouter.ai/api/v1",
            apiFormat = "openai",
            builtinSlot = "openrouter",
            defaultModel = "deepseek/deepseek-chat",
            popularModels = listOf(
                "deepseek/deepseek-chat-v3-0324:free",
                "deepseek/deepseek-chat",
                "deepseek/deepseek-r1:free",
                "meta-llama/llama-3.3-70b-instruct:free",
                "google/gemini-2.0-flash-exp:free",
                "anthropic/claude-3.5-sonnet",
                "qwen/qwen-2.5-72b-instruct"
            ),
            keyHint = "openrouter.ai/keys"
        ),
        CatalogProviderSpec(
            id = "groq",
            name = "Groq Cloud",
            shortDesc = "LPU donanımıyla ultra düşük gecikmeli ve ücretsiz kotalı açık kaynak modeller.",
            badge = "Ultra Hızlı",
            categories = listOf(ApiCatalogCategory.POPULAR, ApiCatalogCategory.FAST, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://api.groq.com/openai/v1",
            apiFormat = "openai",
            builtinSlot = "groq",
            defaultModel = "openai/gpt-oss-120b",
            popularModels = listOf(
                "openai/gpt-oss-120b",
                "openai/gpt-oss-20b",
                "llama-3.3-70b-versatile",
                "qwen/qwen3-32b",
                "gemma2-9b-it"
            ),
            keyHint = "console.groq.com/keys"
        ),
        CatalogProviderSpec(
            id = "deepseek",
            name = "DeepSeek",
            shortDesc = "Doğrudan DeepSeek V3 ve Reasoner API'si. Ekonomik ve güçlü anlatım.",
            badge = "Ekonomik",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.deepseek.com/v1",
            apiFormat = "openai",
            defaultModel = "deepseek-chat",
            popularModels = listOf("deepseek-chat", "deepseek-reasoner"),
            keyHint = "platform.deepseek.com/api_keys"
        ),
        CatalogProviderSpec(
            id = "mistral",
            name = "Mistral AI",
            shortDesc = "Avrupa merkezli, güçlü Türkçe ve yaratıcı hikaye performansına sahip modeller.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.POPULAR, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://api.mistral.ai/v1",
            apiFormat = "openai",
            builtinSlot = "mistral",
            defaultModel = "mistral-large-latest",
            popularModels = listOf(
                "mistral-large-latest",
                "mistral-medium-latest",
                "mistral-small-latest",
                "open-mistral-nemo"
            ),
            keyHint = "console.mistral.ai/api-keys"
        ),
        CatalogProviderSpec(
            id = "nvidia",
            name = "NVIDIA NIM",
            shortDesc = "DeepSeek, Llama 3.3, Qwen ve Mistral için ücretsiz geliştirici kredili sunucular.",
            badge = "Ücretsiz Kredi",
            categories = listOf(ApiCatalogCategory.FREE_TIER, ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://integrate.api.nvidia.com/v1",
            apiFormat = "openai",
            builtinSlot = "nvidia",
            defaultModel = "meta/llama-3.3-70b-instruct",
            popularModels = listOf(
                "meta/llama-3.3-70b-instruct",
                "deepseek-ai/deepseek-r1",
                "qwen/qwen2.5-72b-instruct",
                "mistralai/mistral-large-2-instruct"
            ),
            keyHint = "build.nvidia.com"
        ),
        CatalogProviderSpec(
            id = "xai",
            name = "xAI (Grok)",
            shortDesc = "Grok-3 ve Grok-3 Mini ile özgür ve keskin diyaloglar.",
            badge = "Grok",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.x.ai/v1",
            apiFormat = "openai",
            defaultModel = "grok-3-mini",
            popularModels = listOf("grok-3", "grok-3-mini", "grok-2-latest"),
            keyHint = "console.x.ai"
        ),
        CatalogProviderSpec(
            id = "cerebras",
            name = "Cerebras Inference",
            shortDesc = "Saniyede binlerce token hızında çalışan ücretsiz kotalı Llama ve Qwen API.",
            badge = "Ultra Hızlı",
            categories = listOf(ApiCatalogCategory.FAST, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://api.cerebras.ai/v1",
            apiFormat = "openai",
            defaultModel = "llama-3.3-70b",
            popularModels = listOf("llama-3.3-70b", "qwen-3-32b", "llama3.1-8b"),
            keyHint = "cloud.cerebras.ai"
        ),
        CatalogProviderSpec(
            id = "sambanova",
            name = "SambaNova Cloud",
            shortDesc = "Yüksek hızlı RDU donanımıyla DeepSeek V3/R1 ve Llama 3.3 70B.",
            badge = "Hızlı + Kota",
            categories = listOf(ApiCatalogCategory.FAST, ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://api.sambanova.ai/v1",
            apiFormat = "openai",
            defaultModel = "Meta-Llama-3.3-70B-Instruct",
            popularModels = listOf(
                "Meta-Llama-3.3-70B-Instruct",
                "DeepSeek-V3-0324",
                "DeepSeek-R1",
                "Qwen2.5-72B-Instruct"
            ),
            keyHint = "cloud.sambanova.ai/apis"
        ),
        CatalogProviderSpec(
            id = "together",
            name = "Together AI",
            shortDesc = "Açık kaynak modellerin neredeyse tamamını barındıran güvenilir bulut sağlayıcı.",
            badge = "Çoklu Model",
            categories = listOf(ApiCatalogCategory.AGGREGATOR, ApiCatalogCategory.FAST),
            baseUrl = "https://api.together.xyz/v1",
            apiFormat = "openai",
            defaultModel = "meta-llama/Llama-3.3-70B-Instruct-Turbo",
            popularModels = listOf(
                "meta-llama/Llama-3.3-70B-Instruct-Turbo",
                "deepseek-ai/DeepSeek-V3",
                "Qwen/Qwen2.5-72B-Instruct-Turbo",
                "mistralai/Mixtral-8x22B-Instruct-v0.1"
            ),
            keyHint = "api.together.xyz/settings/api-keys"
        ),
        CatalogProviderSpec(
            id = "fireworks",
            name = "Fireworks AI",
            shortDesc = "Düşük gecikmeli serverless DeepSeek, Llama ve Qwen uç noktaları.",
            badge = "Hızlı",
            categories = listOf(ApiCatalogCategory.FAST, ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.fireworks.ai/inference/v1",
            apiFormat = "openai",
            defaultModel = "accounts/fireworks/models/deepseek-v3",
            popularModels = listOf(
                "accounts/fireworks/models/deepseek-v3",
                "accounts/fireworks/models/llama-v3p3-70b-instruct",
                "accounts/fireworks/models/qwen2p5-72b-instruct"
            ),
            keyHint = "fireworks.ai/account/api-keys"
        ),
        CatalogProviderSpec(
            id = "github_models",
            name = "GitHub Models",
            shortDesc = "GitHub PAT token ile ücretsiz günlük kotalı GPT-4o, DeepSeek ve Llama erişimi.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.FREE_TIER, ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://models.inference.ai.azure.com",
            apiFormat = "openai",
            defaultModel = "gpt-4o-mini",
            popularModels = listOf(
                "gpt-4o",
                "gpt-4o-mini",
                "DeepSeek-V3",
                "Meta-Llama-3.3-70B-Instruct",
                "Mistral-Large-2411"
            ),
            keyHint = "github.com/settings/tokens"
        ),
        CatalogProviderSpec(
            id = "huggingface",
            name = "HuggingFace Router",
            shortDesc = "HuggingFace token'ı ile yüzlerce açık ağırlıklı modeli tek adresten çalıştır.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.FREE_TIER, ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://router.huggingface.co/v1",
            apiFormat = "openai",
            defaultModel = "meta-llama/Llama-3.3-70B-Instruct",
            popularModels = listOf(
                "meta-llama/Llama-3.3-70B-Instruct",
                "Qwen/Qwen2.5-72B-Instruct",
                "deepseek-ai/DeepSeek-R1"
            ),
            keyHint = "huggingface.co/settings/tokens"
        ),
        CatalogProviderSpec(
            id = "deepinfra",
            name = "DeepInfra",
            shortDesc = "Uygun fiyatlı DeepSeek, Llama ve Euryale gibi rol yapma odaklı modeller.",
            badge = "RP & Çoklu",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.deepinfra.com/v1/openai",
            apiFormat = "openai",
            defaultModel = "meta-llama/Llama-3.3-70B-Instruct",
            popularModels = listOf(
                "meta-llama/Llama-3.3-70B-Instruct",
                "deepseek-ai/DeepSeek-V3",
                "Sao10K/L3.3-70B-Euryale-v2.3",
                "Qwen/Qwen2.5-72B-Instruct"
            ),
            keyHint = "deepinfra.com/dash/api_keys"
        ),
        CatalogProviderSpec(
            id = "featherless",
            name = "Featherless AI",
            shortDesc = "Özellikle hikaye ve sansürsüz rol yapma modelleri (Euryale, Rocinante) için.",
            badge = "Rol Yapma",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.featherless.ai/v1",
            apiFormat = "openai",
            defaultModel = "Sao10K/L3.3-70B-Euryale-v2.3",
            popularModels = listOf(
                "Sao10K/L3.3-70B-Euryale-v2.3",
                "TheDrummer/Rocinante-12B-v1.1",
                "meta-llama/Llama-3.3-70B-Instruct"
            ),
            keyHint = "featherless.ai/account/api-keys"
        ),
        CatalogProviderSpec(
            id = "chutes",
            name = "Chutes AI",
            shortDesc = "Merkeziyetsiz GPU ağı üzerinde uygun maliyetli DeepSeek V3/R1 ve Qwen3.",
            badge = "Çoklu Model",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://llm.chutes.ai/v1",
            apiFormat = "openai",
            defaultModel = "deepseek-ai/DeepSeek-V3-0324",
            popularModels = listOf(
                "deepseek-ai/DeepSeek-V3-0324",
                "deepseek-ai/DeepSeek-R1",
                "Qwen/Qwen3-235B-A22B"
            ),
            keyHint = "chutes.ai/app/api"
        ),
        CatalogProviderSpec(
            id = "novita",
            name = "Novita AI",
            shortDesc = "Rol yapma ve genel sohbet için geniş açık model kataloğu.",
            badge = "Çoklu Model",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.novita.ai/v3/openai",
            apiFormat = "openai",
            defaultModel = "deepseek/deepseek-v3-0324",
            popularModels = listOf(
                "deepseek/deepseek-v3-0324",
                "meta-llama/llama-3.3-70b-instruct",
                "sao10k/l3-70b-euryale-v2.1"
            ),
            keyHint = "novita.ai/settings/key-management"
        ),
        CatalogProviderSpec(
            id = "hyperbolic",
            name = "Hyperbolic",
            shortDesc = "Yüksek kaliteli BF16 ağırlıklı DeepSeek, Llama ve Qwen servisleri.",
            badge = "Çoklu Model",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.hyperbolic.xyz/v1",
            apiFormat = "openai",
            defaultModel = "deepseek-ai/DeepSeek-V3",
            popularModels = listOf(
                "deepseek-ai/DeepSeek-V3",
                "meta-llama/Llama-3.3-70B-Instruct",
                "Qwen/Qwen2.5-72B-Instruct"
            ),
            keyHint = "app.hyperbolic.xyz/settings"
        ),
        CatalogProviderSpec(
            id = "perplexity",
            name = "Perplexity AI",
            shortDesc = "Gerçek zamanlı internet bilgisiyle desteklenmiş Sonar modelleri.",
            badge = "Web Bilgili",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.perplexity.ai",
            apiFormat = "openai",
            defaultModel = "sonar",
            popularModels = listOf("sonar-pro", "sonar", "sonar-reasoning-pro"),
            keyHint = "perplexity.ai/settings/api"
        ),
        CatalogProviderSpec(
            id = "cohere",
            name = "Cohere",
            shortDesc = "Çok dilli güçlü Command A ve Command R+ sohbet modelleri.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://api.cohere.ai/compatibility/v1",
            apiFormat = "openai",
            defaultModel = "command-a-03-2025",
            popularModels = listOf(
                "command-a-03-2025",
                "command-r-plus-08-2024",
                "command-r-08-2024"
            ),
            keyHint = "dashboard.cohere.com/api-keys"
        ),
        CatalogProviderSpec(
            id = "qwen_dashscope",
            name = "Alibaba Qwen (DashScope)",
            shortDesc = "Resmi Qwen-Max, Qwen-Plus ve Qwen-Turbo uluslararası uç noktaları.",
            badge = "Qwen Resmi",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
            apiFormat = "openai",
            defaultModel = "qwen-plus",
            popularModels = listOf("qwen-max", "qwen-plus", "qwen-turbo"),
            keyHint = "dashscope.console.aliyun.com"
        ),
        CatalogProviderSpec(
            id = "moonshot",
            name = "Moonshot AI (Kimi)",
            shortDesc = "Uzun bağlam ve yaratıcı diyaloglarda iddialı Kimi K2 ve Moonshot modelleri.",
            badge = "Uzun Bağlam",
            categories = listOf(ApiCatalogCategory.POPULAR),
            baseUrl = "https://api.moonshot.ai/v1",
            apiFormat = "openai",
            defaultModel = "kimi-k2-0711-preview",
            popularModels = listOf("kimi-k2-0711-preview", "moonshot-v1-32k", "moonshot-v1-8k"),
            keyHint = "platform.moonshot.ai/console/api-keys"
        ),
        CatalogProviderSpec(
            id = "zhipu",
            name = "Zhipu AI (GLM)",
            shortDesc = "GLM-4-Flash ücretsiz katmanı ve güçlü GLM-4-Plus modelleri.",
            badge = "Ücretsiz Kota",
            categories = listOf(ApiCatalogCategory.FREE_TIER),
            baseUrl = "https://open.bigmodel.cn/api/paas/v4",
            apiFormat = "openai",
            defaultModel = "glm-4-flash",
            popularModels = listOf("glm-4-flash", "glm-4-plus"),
            keyHint = "open.bigmodel.cn"
        ),
        CatalogProviderSpec(
            id = "siliconflow",
            name = "SiliconFlow",
            shortDesc = "Hızlı ve uygun fiyatlı DeepSeek ve Qwen açık kaynak sunucuları.",
            badge = "Çoklu Model",
            categories = listOf(ApiCatalogCategory.AGGREGATOR),
            baseUrl = "https://api.siliconflow.com/v1",
            apiFormat = "openai",
            defaultModel = "deepseek-ai/DeepSeek-V3",
            popularModels = listOf("deepseek-ai/DeepSeek-V3", "Qwen/Qwen2.5-72B-Instruct"),
            keyHint = "cloud.siliconflow.com"
        ),
        CatalogProviderSpec(
            id = "ollama",
            name = "Ollama (Yerel)",
            shortDesc = "Kendi bilgisayarında veya yerel ağında çalışan Ollama sunucuna bağlan.",
            badge = "Yerel • Ücretsiz",
            categories = listOf(ApiCatalogCategory.LOCAL),
            baseUrl = "http://localhost:11434/v1",
            apiFormat = "openai",
            defaultModel = "llama3.3",
            popularModels = listOf("llama3.3", "qwen2.5:14b", "gemma3:12b", "mistral-nemo"),
            keyHint = "Yerel sunucu için API anahtarı boş bırakılabilir",
            requiresApiKey = false
        ),
        CatalogProviderSpec(
            id = "lmstudio",
            name = "LM Studio (Yerel)",
            shortDesc = "Yerel ağda açık LM Studio OpenAI uyumlu sunucusuna doğrudan bağlan.",
            badge = "Yerel • Ücretsiz",
            categories = listOf(ApiCatalogCategory.LOCAL),
            baseUrl = "http://localhost:1234/v1",
            apiFormat = "openai",
            defaultModel = "local-model",
            popularModels = listOf("local-model"),
            keyHint = "Yerel sunucu için API anahtarı boş bırakılabilir",
            requiresApiKey = false
        )
    )

    fun findById(id: String): CatalogProviderSpec? {
        val clean = id.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return null
        return catalogProviders.firstOrNull {
            it.id.lowercase(Locale.ROOT) == clean || it.builtinSlot?.lowercase(Locale.ROOT) == clean
        }
    }

    fun findFreeKeylessById(id: String): FreeKeylessProviderSpec? {
        val clean = id.trim().lowercase(Locale.ROOT)
        if (clean.isEmpty()) return null
        return freeKeylessProviders.firstOrNull { it.id.lowercase(Locale.ROOT) == clean }
    }

    fun isReasoningOrThinkingModel(modelId: String): Boolean {
        val lower = modelId.trim().lowercase(Locale.ROOT)
        if (lower.isEmpty()) return false
        return lower.contains("deepseek-r1") ||
            lower.contains("reasoner") ||
            lower.contains("thinking") ||
            lower.contains("qwq") ||
            lower.startsWith("o1") ||
            lower.startsWith("o3") ||
            lower.contains("/o1") ||
            lower.contains("/o3")
    }

    fun estimateContextWindowTokens(modelId: String): Int {
        val lower = modelId.trim().lowercase(Locale.ROOT)
        if (lower.isEmpty()) return 32_768
        return when {
            lower.contains("gemini-2.") || lower.contains("gemini-1.5") || lower.contains("gpt-4.1") -> 1_000_000
            lower.contains("claude-3") || lower.contains("claude-sonnet-4") || lower.contains("claude-opus-4") -> 200_000
            lower.contains("moonshot-v1-8k") -> 8_192
            lower.contains("moonshot-v1-32k") -> 32_768
            lower.contains("gpt-4o") ||
                lower.contains("o3") ||
                lower.contains("llama-3.3") ||
                lower.contains("llama-3.1") ||
                lower.contains("deepseek") ||
                lower.contains("qwen") ||
                lower.contains("mistral") ||
                lower.contains("grok-3") ||
                lower.contains("command-a") ||
                lower.contains("command-r") ||
                lower.contains("kimi-k2") ||
                lower.contains("glm-4") -> 128_000
            else -> 32_768
        }
    }

    fun pickPreferredUtilityModel(models: List<String>, fallbackDefault: String): String {
        val cleaned = models.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        val safeFallback = fallbackDefault.trim()
        if (cleaned.isEmpty()) return safeFallback
        val nonReasoning = cleaned.filterNot { isReasoningOrThinkingModel(it) }
        val pool = if (nonReasoning.isNotEmpty()) nonReasoning else cleaned

        val preferredHints = listOf("flash-lite", "flash", "mini", "haiku", "small", "turbo", "8b", "9b", "20b", "12b", "14b", "32b")
        for (hint in preferredHints) {
            val match = pool.firstOrNull { it.lowercase(Locale.ROOT).contains(hint) }
            if (match != null) return match
        }
        return pool.firstOrNull() ?: safeFallback
    }
}

/**
 * Encodes and decodes both the "Prioritize Free Keyless" toggle and the user's
 * ordered API priority list inside `UserSettingsEntity.fallbackChainOrder`
 * without requiring any Room schema change.
 *
 * Format: "free_first:1|gemini,groq,custom_4,openrouter"
 */
object ApiPriorityCodec {
    private const val PREFIX_FREE_ON = "free_first:1|"
    private const val PREFIX_FREE_OFF = "free_first:0|"

    fun isFreeFirst(raw: String): Boolean {
        val trimmed = raw.trim()
        return when {
            trimmed.startsWith(PREFIX_FREE_ON) -> true
            trimmed.startsWith(PREFIX_FREE_OFF) -> false
            trimmed.isBlank() -> false
            else -> {
                // Backward compatibility with legacy chain string
                val first = trimmed.split(",")
                    .firstOrNull { it.isNotBlank() }
                    ?.trim()
                    ?.lowercase(Locale.ROOT)
                first in ApiProviderCatalog.FREE_KEYLESS_IDS
            }
        }
    }

    fun parseOrderedApiKeys(raw: String): List<String> {
        val trimmed = raw.trim()
        val body = when {
            trimmed.startsWith(PREFIX_FREE_ON) -> trimmed.removePrefix(PREFIX_FREE_ON)
            trimmed.startsWith(PREFIX_FREE_OFF) -> trimmed.removePrefix(PREFIX_FREE_OFF)
            else -> trimmed
        }
        return body.split(",")
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter {
                it.isNotBlank() &&
                    it != "main" &&
                    it != "autofallback" &&
                    it !in ApiProviderCatalog.FREE_KEYLESS_IDS
            }
            .distinct()
    }

    fun encode(freeFirst: Boolean, orderedApiKeys: List<String>): String {
        val prefix = if (freeFirst) PREFIX_FREE_ON else PREFIX_FREE_OFF
        val cleanList = orderedApiKeys
            .map { it.trim().lowercase(Locale.ROOT) }
            .filter {
                it.isNotBlank() &&
                    it != "main" &&
                    it != "autofallback" &&
                    it !in ApiProviderCatalog.FREE_KEYLESS_IDS
            }
            .distinct()
        return prefix + cleanList.joinToString(",")
    }

    fun isBuiltinConfigured(slot: String, settings: UserSettingsEntity): Boolean = when (slot.trim().lowercase(Locale.ROOT)) {
        "gemini" -> settings.customApiKey.isNotBlank()
        "groq" -> settings.groqApiKey.isNotBlank()
        "openai" -> settings.openaiApiKey.isNotBlank()
        "claude" -> settings.claudeApiKey.isNotBlank()
        "openrouter" -> settings.openRouterApiKey.isNotBlank()
        "nvidia" -> settings.nvidiaApiKey.isNotBlank()
        "mistral" -> settings.mistralApiKey.isNotBlank()
        else -> false
    }

    fun resolveOrderedConfiguredKeyedProviders(
        settings: UserSettingsEntity,
        customProviders: List<CustomProviderEntity>
    ): List<String> {
        val customIds = customProviders.map { "custom_${it.id}" }.toSet()
        val configuredSet = linkedSetOf<String>()

        for (slot in ApiProviderCatalog.BUILTIN_KEYED_SLOTS) {
            if (isBuiltinConfigured(slot, settings)) {
                configuredSet.add(slot)
            }
        }
        for (cp in customProviders) {
            configuredSet.add("custom_${cp.id}")
        }

        val savedOrder = parseOrderedApiKeys(settings.fallbackChainOrder)
        val result = linkedSetOf<String>()

        // 1. Respect user's explicit priority order first
        for (key in savedOrder) {
            if (key in configuredSet || key in customIds) {
                result.add(key)
            }
        }

        // 2. If user had a selectedProvider that's configured and wasn't in savedOrder, place it next
        val selected = settings.selectedProvider.trim().lowercase(Locale.ROOT)
        if (selected in configuredSet && selected !in result) {
            result.add(selected)
        }

        // 3. Append any remaining configured providers deterministically
        for (key in configuredSet) {
            if (key !in result) result.add(key)
        }

        return result.toList()
    }
}
