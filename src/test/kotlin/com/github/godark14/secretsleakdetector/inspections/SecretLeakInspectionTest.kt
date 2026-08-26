package com.github.godark14.secretsleakdetector.inspections

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class SecretLeakInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(SecretLeakInspection())
    }

    fun testDetectsAwsAccessKey() {
        myFixture.configureByText(
            "test.env",
            "AWS_ACCESS_KEY=<warning descr=\"Potential hardcoded secret detected: AWS Access Key\">AKIAIOSFODNN7EXAMPLE</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsGenericApiKey() {
        myFixture.configureByText(
            "test.env",
            "api_key = <warning descr=\"Potential hardcoded secret detected: Generic API Key\">\"sk_test_51H8xK2LqRz9mNpQwErTyUiOp\"</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsGithubToken() {
        myFixture.configureByText(
            "test.env",
            "GITHUB_TOKEN=<warning descr=\"Potential hardcoded secret detected: GitHub Token\">ghp_1234567890abcdefghijklmnopqrstuvwxyz</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsRsaPrivateKey() {
        myFixture.configureByText(
            "id_rsa",
            "<warning descr=\"Potential hardcoded secret detected: RSA Private Key\">-----BEGIN RSA PRIVATE KEY-----</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDoesNotFlagOrdinaryText() {
        myFixture.configureByText(
            "notes.txt",
            "This is just a regular sentence with no secrets in it."
        )
        myFixture.testHighlighting(true, false, false)
    }
    fun testDoesNotFlagPlaceholderApiKey() {
        myFixture.configureByText(
            "test.env",
            "api_key = \"YOUR_API_KEY_HERE_1234567\""
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDoesNotFlagRepeatedCharacterValue() {
        myFixture.configureByText(
            "test.env",
            "api_key = \"xxxxxxxxxxxxxxxxxxxxxxxx\""
        )
        myFixture.testHighlighting(true, false, false)
    }
    fun testDoesNotFlagLowEntropyValue() {
        myFixture.configureByText(
            "test.env",
            "api_key = \"aaaaaaaabbbbbbbbccccccccdddddddd\""
        )
        myFixture.testHighlighting(true, false, false)
    }
    fun testQuickFixReplacesSecretWithPlaceholder() {
        myFixture.configureByText(
            "test.env",
            "AWS_ACCESS_KEY=<caret>AKIAIOSFODNN7EXAMPLE"
        )
        val intention = myFixture.findSingleIntention("Replace with placeholder")
        myFixture.launchAction(intention)
        myFixture.checkResult("AWS_ACCESS_KEY=REDACTED_SECRET")
    }
    fun testDetectsGoogleCloudApiKey() {
        myFixture.configureByText(
            "test.env",
            "GOOGLE_API_KEY=<warning descr=\"Potential hardcoded secret detected: Google Cloud API Key\">AIzaSyD-9tSrke72PouQMnMX-a7eZSW0jkFMBWY</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsStripeSecretKey() {
        myFixture.configureByText(
            "test.env",
            "STRIPE_KEY=<warning descr=\"Potential hardcoded secret detected: Stripe Secret Key\">sk_live_4eC39HqLyjWDarjtT1zdp7dc</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsJwt() {
        myFixture.configureByText(
            "test.env",
            "TOKEN=<warning descr=\"Potential hardcoded secret detected: JWT\">eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIn0.dGhpc2lzYWZha2VzaWduYXR1cmU</warning>"
        )
        myFixture.testHighlighting(true, false, false)
    }

    fun testDetectsDatabaseConnectionStringPassword() {
        myFixture.configureByText(
            "test.env",
            "DB_URL=postgres://admin:<warning descr=\"Potential hardcoded secret detected: Database Connection String with Password\">S3cr3tP4ss</warning>@db.example.com:5432/mydb"
        )
        myFixture.testHighlighting(true, false, false)
    }
}