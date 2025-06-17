import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.random.Random

/**
 * Exception thrown when a CPF is invalid.
 *
 * @param message A message describing the validation error.
 */
class InvalidCPFException(message: String? = null): RuntimeException(message)

/**
 * Converts this [String] into a valid [CPF], or throws [InvalidCPFException] if invalid.
 *
 * @throws InvalidCPFException if the string is not a valid CPF.
 */
fun String.asCPF() = CPF.parse(this)

/**
 * Converts this [String] into a valid [CPF], or returns `null` if it's invalid.
 */
fun String.asCPFOrNull(): CPF? = runCatching { CPF.parse(this) }.getOrNull()

/**
 * Represents a validated CPF (Cadastro de Pessoa Física), which is the Brazilian individual taxpayer registry.
 *
 * This is a value object that ensures only valid CPF numbers can be instantiated.
 *
 * Use [CPF.parse] or [String.asCPF] to create instances.
 *
 * @property value The unformatted 11-digit CPF string.
 */
@Serializable(with = CPFSerializer::class)
class CPF private constructor(
    private  val value: String
): Comparable<CPF>{

    override fun compareTo(other: CPF): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CPF
        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = raw()

    /**
     * Returns the unformatted CPF string (digits only).
     */
    fun raw(): String = value

    /**
     * Returns the CPF formatted as `XXX.XXX.XXX-XX`.
     */
    fun formatted(): String = buildString {
        for (i in value.indices) {
            if (i == 3 || i == 6) append(".")
            if (i == 9) append("-")
            append(value[i])
        }
    }

    companion object {
        private val nonDigitsRegex = "\\D".toRegex()

        /**
         * Parses and validates a CPF string (with or without formatting).
         *
         * @param cpfString The input string to parse.
         * @return A validated [CPF] instance.
         * @throws InvalidCPFException if the input is not a valid CPF.
         */
        fun parse(cpfString: String): CPF {
            val cleanCPF = cpfString.replace(nonDigitsRegex, "")

            if (cleanCPF.length != 11)
                throw InvalidCPFException("Invalid CPF '$cpfString': must contain exactly 11 digits")

            if (cleanCPF.all { it == cleanCPF[0] })
                throw InvalidCPFException("Invalid CPF '$cpfString': all digits are equal")

            val cpfDigits = cleanCPF.map { it.digitToInt()}

            val firstDigits = cpfDigits.take(9)
            val lastDigits = cpfDigits.takeLast(2)

            val firstVerifier = digitVerifier(firstDigits)
            if (firstVerifier != lastDigits[0])
                throw InvalidCPFException("Invalid CPF '$cpfString': first verifier digit should be $firstVerifier")

            val lastVerifier = digitVerifier(firstDigits + firstVerifier)
            if (lastVerifier != lastDigits[1])
                throw InvalidCPFException("Invalid CPF '$cpfString': second verifier digit should be $lastVerifier")


            return CPF(cleanCPF)
        }

        /**
         * Generates a valid, random CPF.
         *
         * @param random The random generator to use (default: [Random.Default]).
         * @return A valid random [CPF].
         */
        fun random(random: Random = Random.Default): CPF {
            val firstDigits = List(9) { random.nextInt(0, 10) }
            val firstVerifier = digitVerifier(firstDigits)
            val lastVerifier = digitVerifier(firstDigits + firstVerifier)

            return CPF((firstDigits + firstVerifier + lastVerifier).joinToString(""))
        }

        /**
         * Calculates the CPF verification digit based on the given digits.
         *
         * @param digits The list of digits to calculate the verifier from.
         * @return The calculated verifier digit.
         */
        private fun digitVerifier(digits: List<Int>): Int {
            val weight = digits.size + 1

            val sum = digits.mapIndexed { i, digit ->
                digit * (weight - i)
            }.sum()

            val rest = sum % 11

            return if (rest < 2 ) 0 else 11 - rest
        }
    }
}

/**
 * Custom serializer for the [CPF] class used by kotlinx.serialization.
 *
 * Serializes the CPF as a plain 11-digit string without formatting.
 * Automatically validates CPF on deserialization.
 */

object CPFSerializer : KSerializer<CPF> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CPF", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: CPF) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): CPF {
        return CPF.parse(decoder.decodeString())
    }
}
