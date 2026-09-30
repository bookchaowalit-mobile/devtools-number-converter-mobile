package com.bookchaowalit.numberconverter

import java.math.BigInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NumberConverterPass3Test {

    @Test
    fun decimalOutputRoundTripsThroughParse() {
        // allBases() shows DEC as "1,234,567"; copying it back into the input must work.
        val dec = NumberConverter.allBases("12D687", 16).getValue("DEC")
        assertEquals("1,234,567", dec)
        assertEquals(BigInteger.valueOf(1_234_567), NumberConverter.parse(dec, 10))
        assertEquals(BigInteger.valueOf(-1_000), NumberConverter.parse("-1,000", 10))
    }

    @Test
    fun commasThatAreNotThousandsGroupingAreRejected() {
        // "1,5" is a decimal comma in many locales, not fifteen.
        assertFailsWith<ConversionException> { NumberConverter.parse("1,5", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1,2345", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse(",123", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1,,234", 10) }
        assertFailsWith<ConversionException> { NumberConverter.parse("1,234", 16) }
    }

    @Test
    fun unicodeSpacesZeroWidthAndMinusSignAreHandled() {
        assertEquals(BigInteger.valueOf(1_000_000), NumberConverter.parse("1 000 000", 10))
        assertEquals(BigInteger.valueOf(255), NumberConverter.parse("﻿ff​", 16))
        assertEquals(BigInteger.valueOf(-42), NumberConverter.parse("−42", 10))
    }
}
