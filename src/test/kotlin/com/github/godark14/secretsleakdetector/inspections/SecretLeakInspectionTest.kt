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
}