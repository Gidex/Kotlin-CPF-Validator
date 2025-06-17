# Kotlin CPF Validator

## Overview
This Kotlin library provides a robust implementation for validating and handling Brazilian CPF (Cadastro de Pessoa Física) numbers. It ensures that only valid CPF numbers can be instantiated, with support for parsing, formatting, random generation, and serialization.

## Features
- **Validation**: Validates CPF numbers, ensuring they meet the Brazilian tax registry's requirements (11 digits, valid verifier digits).
- **Parsing**: Converts a string (with or without formatting) into a validated `CPF` object.
- **Formatting**: Provides both raw (digits only) and formatted (`XXX.XXX.XXX-XX`) representations.
- **Random Generation**: Generates valid random CPF numbers for testing purposes.
- **Serialization**: Supports serialization/deserialization of `CPF` objects using `kotlinx.serialization`.
- **Type Safety**: Uses a value object (`CPF`) to ensure only valid CPFs are created.
- **Exception Handling**: Throws `InvalidCPFException` for invalid CPF inputs.

## Installation
1. Add the library to your project by including the source file.
2. Ensure you have `kotlinx.serialization` in your project dependencies:
   ```kotlin
   // build.gradle.kts
   implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:<version>")
   ```

## Usage
### Creating a CPF
Use `CPF.parse` or the `String.asCPF` extension function to create a validated `CPF` object:
```kotlin
val cpf = "123.456.789-09".asCPF() // Throws InvalidCPFException if invalid
println(cpf.formatted()) // Output: 123.456.789-09
println(cpf.raw()) // Output: 12345678909
```

### Safe Parsing
Use `String.asCPFOrNull` to parse a CPF safely without throwing exceptions:
```kotlin
val cpf = "invalid-cpf".asCPFOrNull() // Returns null if invalid
println(cpf) // Output: null
```

### Generating Random CPFs
Generate valid random CPFs for testing:
```kotlin
val randomCPF = CPF.random()
println(randomCPF.formatted()) // Output: e.g., 987.654.321-00
```

### Serialization
The `CPF` class is serializable with `kotlinx.serialization`:
```kotlin
val cpf = CPF.parse("12345678909")
val json = Json.encodeToString(cpf) // Serializes to "12345678909"
val deserialized = Json.decodeFromString<CPF>(json) // Deserializes back to CPF
```

### Error Handling
Invalid CPFs throw an `InvalidCPFException`:
```kotlin
try {
    val cpf = CPF.parse("111.111.111-11") // All digits equal
} catch (e: InvalidCPFException) {
    println(e.message) // Output: Invalid CPF '111.111.111-11': all digits are equal
}
```

## Code Structure
- **Package**: `dev.gidex.domain.utils`
- **Main Class**: `CPF`
  - Represents a validated CPF with methods for raw and formatted output.
  - Implements `Comparable<CPF>` for sorting and comparison.
- **Extension Functions**:
  - `String.asCPF()`: Converts a string to a `CPF` or throws `InvalidCPFException`.
  - `String.asCPFOrNull()`: Converts a string to a `CPF` or returns `null` if invalid.
- **Companion Object**: Provides parsing (`parse`) and random generation (`random`) utilities.
- **Serializer**: `CPFSerializer` for `kotlinx.serialization` support.
- **Exception**: `InvalidCPFException` for invalid CPF inputs.

## Requirements
- Kotlin 1.9.0 or higher
- `kotlinx.serialization` for serialization support

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for bugs, improvements, or feature requests.
