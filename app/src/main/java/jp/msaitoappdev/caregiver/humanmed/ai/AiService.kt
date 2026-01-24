
package jp.msaitoappdev.caregiver.humanmed.ai

import jp.msaitoappdev.caregiver.humanmed.domain.model.Question

interface AiService {
    suspend fun easyExplain(q: Question): String
}

class LocalAiService : AiService {
    override suspend fun easyExplain(q: Question): String = "やさしい説明（ダミー）。設定 > プレミアムで有効化してください。"
}
