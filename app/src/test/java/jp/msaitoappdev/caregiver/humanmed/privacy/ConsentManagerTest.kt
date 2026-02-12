package jp.msaitoappdev.caregiver.humanmed.privacy

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ConsentManagerTest {

    private val activity: Activity = mockk(relaxed = true)
    private val onReady: () -> Unit = mockk(relaxed = true)
    private val consentInformation: ConsentInformation = mockk(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(UserMessagingPlatform::class)
        every { UserMessagingPlatform.getConsentInformation(any()) } returns consentInformation
    }

    @After
    fun tearDown() {
        unmockkStatic(UserMessagingPlatform::class)
    }

    @Ignore("This test is unstable due to the complexity of mocking UMP SDK.")
    @Test
    fun `obtain sequence success`() {
        // Arrange
        val requestSuccessListener = slot<ConsentInformation.OnConsentInfoUpdateSuccessListener>()
        val loadAndShowListener = slot<(FormError?) -> Unit>()

        every {
            consentInformation.requestConsentInfoUpdate(
                any(),
                any(),
                capture(requestSuccessListener),
                any()
            )
        } answers {
            requestSuccessListener.captured.onConsentInfoUpdateSuccess()
        }

        every {
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, capture(loadAndShowListener))
        } answers {
            loadAndShowListener.captured.invoke(null)
        }

        // Act
        ConsentManager.obtain(activity, onReady)

        // Assert
        verify { UserMessagingPlatform.getConsentInformation(activity) }
        verify { consentInformation.requestConsentInfoUpdate(any(), any(), any(), any()) }
        verify { UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity, any()) }
        verify { onReady() }
    }

    @Ignore("This test is unstable due to the complexity of mocking UMP SDK.")
    @Test
    fun `obtain calls onReady even on request failure`() {
        // Arrange
        val requestFailureListener = slot<ConsentInformation.OnConsentInfoUpdateFailureListener>()
        val mockError = mockk<FormError>()

        every {
            consentInformation.requestConsentInfoUpdate(
                any(),
                any(),
                any(),
                capture(requestFailureListener)
            )
        } answers {
            requestFailureListener.captured.onConsentInfoUpdateFailure(mockError)
        }

        // Act
        ConsentManager.obtain(activity, onReady)

        // Assert
        verify { onReady() }
        verify(exactly = 0) { UserMessagingPlatform.loadAndShowConsentFormIfRequired(any(), any()) }
    }
}
