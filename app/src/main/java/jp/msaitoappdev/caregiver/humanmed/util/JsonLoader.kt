
package jp.msaitoappdev.caregiver.humanmed.util

import kotlinx.serialization.json.Json

object JsonLoader {
    val json = Json { ignoreUnknownKeys = true }
}
