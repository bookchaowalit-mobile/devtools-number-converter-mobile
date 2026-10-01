package com.bookchaowalit.numberconverter

import java.math.BigInteger

class ConversionException(message: String) : IllegalArgumentException(message)

object NumberConverter {
    const val MIN_RADIX = 2
    const val MAX_RADIX = 36

    private fun checkRadix(radix: Int) {
        if (radix !in MIN_RADIX..MAX_RADIX) throw ConversionException("radix must be in $MIN_RADIX..$MAX_RADIX, was $radix")
    }

    /**
     * Parses [input] in [radix]. Accepts an optional sign (including the
     * Unicode minus U+2212), `0x`/`0b`/`0o` prefixes matching the radix, `_`
     * and any Unicode space character (NBSP, thin space...; not tabs or line
     * breaks) as digit separators, and in
     * base 10 comma thousands grouping ("1,234,567", as [allBases] displays
     * it). Zero-width characters and a BOM are ignored.
     */
    fun parse(input: String, radix: Int): BigInteger {
        checkRadix(radix)
        var s = input.filterNot { it in INVISIBLE }.trim()
            .filterNot { it == '_' || Character.getType(it) == Character.SPACE_SEPARATOR.toInt() }
            .replace('\u2212', '-')
        var negative = false
        if (s.startsWith("-") || s.startsWith("+")) {
            negative = s[0] == '-'
            s = s.substring(1)
        }
        if (radix == 10 && ',' in s) {
            // Only strict thousands grouping: "1,5" is a decimal comma, not 15.
            val groups = s.split(',')
            val ok = groups.first().length in 1..3 && groups.drop(1).all { it.length == 3 }
            if (!ok) throw ConversionException("\"$input\" is not a whole number with 3-digit comma grouping")
            s = groups.joinToString("")
        }
        val prefix = when (radix) { 16 -> "0x"; 2 -> "0b"; 8 -> "0o"; else -> null }
        if (prefix != null && s.lowercase().startsWith(prefix)) s = s.substring(2)
        if (s.isEmpty()) throw ConversionException("no digits in \"$input\"")
        val bad = s.firstOrNull { Character.digit(it, radix) < 0 }
        if (bad != null) throw ConversionException("'$bad' is not a valid base-$radix digit")
        val value = BigInteger(s, radix)
        return if (negative) value.negate() else value
    }

    /** Formats [value] in [radix] using upper-case digits, optionally grouped. */
    fun format(value: BigInteger, radix: Int, groupSize: Int = 0, separator: Char = ' '): String {
        checkRadix(radix)
        require(groupSize >= 0) { "groupSize must be >= 0" }
        val digits = value.abs().toString(radix).uppercase()
        val grouped = if (groupSize == 0) digits else digits.reversed().chunked(groupSize).joinToString(separator.toString()).reversed()
        return if (value.signum() < 0) "-$grouped" else grouped
    }

    private val INVISIBLE = setOf('\u200B', '\u200C', '\u200D', '\u2060', '\uFEFF')

    fun convert(input: String, fromRadix: Int, toRadix: Int): String = format(parse(input, fromRadix), toRadix)

    /** The common bases shown side by side in the app. */
    fun allBases(input: String, fromRadix: Int): Map<String, String> {
        val v = parse(input, fromRadix)
        return linkedMapOf(
            "BIN" to format(v, 2, groupSize = 4),
            "OCT" to format(v, 8),
            "DEC" to format(v, 10, groupSize = 3, separator = ','),
            "HEX" to format(v, 16, groupSize = 4),
        )
    }

    /**
     * Two's complement bit pattern of [value] in [bits] bits (8/16/32/64...).
     * Throws when the value does not fit.
     */
    fun twosComplement(value: BigInteger, bits: Int): String {
        require(bits > 0) { "bits must be positive" }
        val min = BigInteger.ONE.shiftLeft(bits - 1).negate()
        val max = BigInteger.ONE.shiftLeft(bits - 1).subtract(BigInteger.ONE)
        if (value < min || value > max) throw ConversionException("$value does not fit in $bits-bit signed")
        val unsigned = if (value.signum() < 0) value.add(BigInteger.ONE.shiftLeft(bits)) else value
        return unsigned.toString(2).padStart(bits, '0')
    }

    private val ROMAN = listOf(
        1000 to "M", 900 to "CM", 500 to "D", 400 to "CD", 100 to "C", 90 to "XC",
        50 to "L", 40 to "XL", 10 to "X", 9 to "IX", 5 to "V", 4 to "IV", 1 to "I",
    )

    fun toRoman(n: Int): String {
        if (n !in 1..3999) throw ConversionException("roman numerals cover 1..3999")
        var rest = n
        return buildString {
            for ((v, sym) in ROMAN) while (rest >= v) { append(sym); rest -= v }
        }
    }

    /** Strict parser: rejects non-canonical forms such as "IIII" or "VX". */
    fun fromRoman(input: String): Int {
        val s = input.trim().uppercase()
        if (s.isEmpty()) throw ConversionException("empty roman numeral")
        var i = 0
        var total = 0
        for ((v, sym) in ROMAN) {
            while (s.startsWith(sym, i)) { total += v; i += sym.length }
        }
        if (i != s.length || total !in 1..3999 || toRoman(total) != s) throw ConversionException("invalid roman numeral \"$input\"")
        return total
    }
}
