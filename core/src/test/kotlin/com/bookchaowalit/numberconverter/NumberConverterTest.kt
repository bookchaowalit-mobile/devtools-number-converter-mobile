package com.bookchaowalit.numberconverter

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NumberConverterTest {
    @Test
    fun convertsBetweenBases() {
        assertEquals("FF", NumberConverter.convert("255", 10, 16))
        assertEquals("11111111", NumberConverter.convert("0xff", 16, 2))
        assertEquals("-10", NumberConverter.convert("-0b1010", 2, 10))
        assertEquals("Z", NumberConverter.convert("35", 10, 36))
        assertEquals("1000", NumberConverter.convert("1_000", 10, 10))
    }

    @Test
    fun handlesNumbersBeyondLong() {
        val big = "340282366920938463463374607431768211455" // 2^128 - 1
        assertEquals("F".repeat(32), NumberConverter.convert(big, 10, 16))
    }

    @Test
    fun rejectsInvalidInput() {
        assertFailsWith<ConversionException> { NumberConverter.parse("102", 2) }
        assertFailsWith<ConversionException> { NumberConverter.parse("", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("0x", 16) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1", 37) }
        assertFailsWith<ConversionException> { NumberConverter.parse("--1", 10) }
    }

    @Test
    fun groupsDigits() {
        val bases = NumberConverter.allBases("1234567", 10)
        assertEquals("1,234,567", bases["DEC"])
        assertEquals("12 D687", bases["HEX"])
        assertEquals("1 0010 1101 0110 1000 0111", bases["BIN"])
        assertEquals("4553207", bases["OCT"])
        assertEquals("-1,000", NumberConverter.format(BigInteger.valueOf(-1000), 10, 3, ','))
    }

    @Test
    fun twosComplement() {
        assertEquals("11111111", NumberConverter.twosComplement(BigInteger.valueOf(-1), 8))
        assertEquals("10000000", NumberConverter.twosComplement(BigInteger.valueOf(-128), 8))
        assertEquals("01111111", NumberConverter.twosComplement(BigInteger.valueOf(127), 8))
        assertFailsWith<ConversionException> { NumberConverter.twosComplement(BigInteger.valueOf(128), 8) }
    }

    @Test
    fun romanNumeralsRoundTrip() {
        assertEquals("MCMXCIV", NumberConverter.toRoman(1994))
        assertEquals("MMMCMXCIX", NumberConverter.toRoman(3999))
        for (n in 1..3999) assertEquals(n, NumberConverter.fromRoman(NumberConverter.toRoman(n)))
        assertEquals(4, NumberConverter.fromRoman(" iv "))
        assertFailsWith<ConversionException> { NumberConverter.fromRoman("IIII") }
        assertFailsWith<ConversionException> { NumberConverter.fromRoman("VX") }
        assertFailsWith<ConversionException> { NumberConverter.fromRoman("ABC") }
        assertFailsWith<ConversionException> { NumberConverter.toRoman(0) }
    }
}
