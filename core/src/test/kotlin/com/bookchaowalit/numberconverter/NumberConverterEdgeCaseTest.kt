package com.bookchaowalit.numberconverter

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NumberConverterEdgeCaseTest {

    @Test
    fun zeroAndSignedZero() {
        assertEquals("0", NumberConverter.convert("0", 10, 2))
        assertEquals("0", NumberConverter.convert("-0", 10, 16))
        assertEquals("0", NumberConverter.convert("+0x0", 16, 10))
        assertEquals(mapOf("BIN" to "0", "OCT" to "0", "DEC" to "0", "HEX" to "0"), NumberConverter.allBases("0", 10))
    }

    @Test
    fun prefixesAreCaseInsensitiveAndOnlyForTheirRadix() {
        assertEquals("255", NumberConverter.convert("0XFF", 16, 10))
        assertEquals("5", NumberConverter.convert("0B101", 2, 10))
        assertEquals("8", NumberConverter.convert("0o10", 8, 10))
        // In base 16, "0b1" is the hex number 0xB1, not a binary prefix.
        assertEquals("177", NumberConverter.convert("0b1", 16, 10))
        assertFailsWith<ConversionException> { NumberConverter.parse("0x10", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("0x-5", 16) }
    }

    @Test
    fun separatorsAndWhitespace() {
        assertEquals("1000000", NumberConverter.convert(" 1 000_000 ", 10, 10))
        assertEquals("-5", NumberConverter.convert("- 5", 10, 10))
        assertFailsWith<ConversionException> { NumberConverter.parse("_ _", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1\t0", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1.5", 10) }
    }

    @Test
    fun thaiAndFullWidthDigitsAreAccepted() {
        // Character.digit understands Unicode decimal digits; useful for Thai input.
        assertEquals("2026", NumberConverter.convert("๒๐๒๖", 10, 10))
        assertEquals("FF", NumberConverter.convert("２５５", 10, 16))
    }

    @Test
    fun formatEdgeCases() {
        assertEquals("ZZ", NumberConverter.format(BigInteger.valueOf(1295), 36))
        assertEquals("123", NumberConverter.format(BigInteger.valueOf(123), 10, groupSize = 10))
        assertEquals("-1 0000", NumberConverter.format(BigInteger.valueOf(-16), 2, groupSize = 4))
        assertFailsWith<IllegalArgumentException> { NumberConverter.format(BigInteger.ONE, 10, groupSize = -1) }
        assertFailsWith<ConversionException> { NumberConverter.format(BigInteger.ONE, 1) }
    }

    @Test
    fun twosComplementBounds() {
        val longMin = BigInteger.valueOf(Long.MIN_VALUE)
        assertEquals("1" + "0".repeat(63), NumberConverter.twosComplement(longMin, 64))
        assertEquals("0" + "1".repeat(63), NumberConverter.twosComplement(BigInteger.valueOf(Long.MAX_VALUE), 64))
        assertFailsWith<ConversionException> { NumberConverter.twosComplement(longMin.subtract(BigInteger.ONE), 64) }
        assertEquals("1", NumberConverter.twosComplement(BigInteger.valueOf(-1), 1))
        assertEquals("0", NumberConverter.twosComplement(BigInteger.ZERO, 1))
        assertFailsWith<ConversionException> { NumberConverter.twosComplement(BigInteger.ONE, 1) }
        assertFailsWith<IllegalArgumentException> { NumberConverter.twosComplement(BigInteger.ZERO, 0) }
    }

    @Test
    fun romanRejectsNonCanonicalAndOutOfRange() {
        for (bad in listOf("", "  ", "MMMM", "IM", "IC", "XM", "VV", "LL", "DD", "CMM", "IIV", "XIIII", "I V")) {
            assertFailsWith<ConversionException>(bad) { NumberConverter.fromRoman(bad) }
        }
        assertEquals(3888, NumberConverter.fromRoman("mmmdccclxxxviii"))
        assertFailsWith<ConversionException> { NumberConverter.toRoman(4000) }
        assertFailsWith<ConversionException> { NumberConverter.toRoman(-1) }
    }
}
