package org.psei.validation

/**
 * Lightweight input validation framework (equivalent to Zod/Joi/Valibot).
 *
 * Provides composable validators that can be chained and applied to request
 * payloads before they reach business logic. Validates types, ranges, formats,
 * and custom constraints with descriptive error messages.
 */

// ─── Core Validation Types ────────────────────────────────────────────────────

sealed class ValidationRule<in T> {
    abstract fun validate(value: T): Result<Unit>
}

data class ValidationError(
    val field: String,
    val message: String,
    val code: String = "INVALID"
)

data class ValidationResult(
    val valid: Boolean,
    val errors: List<ValidationError> = emptyList(),
    val sanitizedValue: Any? = null
) {
    companion object {
        fun success(sanitizedValue: Any? = null) = ValidationResult(true, sanitizedValue = sanitizedValue)
        fun failure(errors: List<ValidationError>) = ValidationResult(false, errors)
    }
}

// ─── Primitive Validators ─────────────────────────────────────────────────────

class StringValidator {
    fun nonBlank(message: String = "Must not be blank") = StringRule.NonBlank(message)
    fun minLength(min: Int, message: String = "Must be at least $min characters") = StringRule.MinLength(min, message)
    fun maxLength(max: Int, message: String = "Must be at most $max characters") = StringRule.MaxLength(max, message)
    fun matches(regex: Regex, message: String = "Does not match expected format") = StringRule.Matches(regex, message)
    fun email(message: String = "Invalid email format") = StringRule.Email(message)
    fun phone(message: String = "Invalid phone format") = StringRule.Phone(message)
    fun pan(message: String = "Invalid PAN format") = StringRule.Pan(message)
    fun aadhar(message: String = "Invalid Aadhar format") = StringRule.Aadhar(message)
}

sealed class StringRule(
    private val message: String
) : ValidationRule<String>() {
    data class NonBlank(private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (value.isBlank()) Result.failure(IllegalArgumentException(msg)) else Result.success(Unit)
    }
    data class MinLength(private val min: Int, private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (value.length < min) Result.failure(IllegalArgumentException(msg)) else Result.success(Unit)
    }
    data class MaxLength(private val max: Int, private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (value.length > max) Result.failure(IllegalArgumentException(msg)) else Result.success(Unit)
    }
    data class Matches(private val regex: Regex, private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (regex.matches(value)) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Email(private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (EMAIL_REGEX.matches(value)) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Phone(private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (PHONE_REGEX.matches(value)) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Pan(private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (PAN_REGEX.matches(value)) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Aadhar(private val msg: String) : StringRule(msg) {
        override fun validate(value: String) = if (AADHAR_REGEX.matches(value)) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
}

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
private val PHONE_REGEX = Regex("^\\+?[1-9]\\d{6,14}$")
private val PAN_REGEX = Regex("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
private val AADHAR_REGEX = Regex("^[2-9]\\d{11}$")

class NumberValidator {
    fun positive(message: String = "Must be positive") = NumberRule.Positive(message)
    fun min(min: Long, message: String = "Must be at least $min") = NumberRule.Min(min, message)
    fun max(max: Long, message: String = "Must be at most $max") = NumberRule.Max(max, message)
    fun range(min: Long, max: Long, message: String = "Must be between $min and $max") = NumberRule.Range(min, max, message)
}

sealed class NumberRule(
    private val message: String
) : ValidationRule<Long>() {
    data class Positive(private val msg: String) : NumberRule(msg) {
        override fun validate(value: Long) = if (value > 0) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Min(private val min: Long, private val msg: String) : NumberRule(msg) {
        override fun validate(value: Long) = if (value >= min) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Max(private val max: Long, private val msg: String) : NumberRule(msg) {
        override fun validate(value: Long) = if (value <= max) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
    data class Range(private val min: Long, private val max: Long, private val msg: String) : NumberRule(msg) {
        override fun validate(value: Long) = if (value in min..max) Result.success(Unit) else Result.failure(IllegalArgumentException(msg))
    }
}

// ─── Composite Schema Validator ───────────────────────────────────────────────

data class FieldValidation<T>(
    val fieldName: String,
    val rules: List<ValidationRule<T>>
) {
    fun validate(value: T): List<ValidationError> = rules.mapNotNull { rule ->
        rule.validate(value).exceptionOrNull()?.let { ValidationError(fieldName, it.message ?: "Invalid") }
    }
}

class SchemaValidator {
    private val fieldValidations = mutableListOf<FieldValidation<*>>()

    fun addStringField(fieldName: String, vararg rules: StringRule): SchemaValidator {
        fieldValidations.add(FieldValidation(fieldName, rules.toList()))
        return this
    }

    fun addNumberField(fieldName: String, vararg rules: NumberRule): SchemaValidator {
        fieldValidations.add(FieldValidation(fieldName, rules.toList()))
        return this
    }

    fun addRequiredField(fieldName: String, message: String = "Field is required"): SchemaValidator {
        fieldValidations.add(FieldValidation(fieldName, listOf(StringRule.NonBlank(message))))
        return this
    }

    fun validateMap(data: Map<String, String?>): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        for (fv in fieldValidations) {
            val value = data[fv.fieldName]
            if (value == null) {
                errors.add(ValidationError(fv.fieldName, "Field is required", "REQUIRED"))
            } else {
                when (fv) {
                    is FieldValidation<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val typedRules = fv.rules as List<ValidationRule<String>>
                        for (rule in typedRules) {
                            rule.validate(value).exceptionOrNull()?.let {
                                errors.add(ValidationError(fv.fieldName, it.message ?: "Invalid"))
                            }
                        }
                    }
                }
            }
        }
        return if (errors.isEmpty()) ValidationResult.success() else ValidationResult.failure(errors)
    }
}

// ─── Validation Helpers (DSL-friendly) ────────────────────────────────────────

object V {
    val string = StringValidator()
    val number = NumberValidator()

    fun schema(name: String, block: SchemaValidator.() -> Unit): SchemaValidator {
        val validator = SchemaValidator()
        validator.block()
        return validator
    }
}

// ─── Common Validation Schemas for PSEI Domain ────────────────────────────────

object PseiValidation {

    // User Registration
    val registrationSchema = V.schema("registration") {
        addStringField("email", V.string.email())
        addStringField("password", V.string.minLength(8))
        addStringField("fullName", V.string.nonBlank())
    }

    // User Login
    val loginSchema = V.schema("login") {
        addStringField("email", V.string.email())
        addStringField("password", V.string.nonBlank())
    }

    // Idea Creation
    val ideaSchema = V.schema("idea") {
        addStringField("title", V.string.nonBlank(), V.string.minLength(3), V.string.maxLength(200))
        addStringField("description", V.string.nonBlank(), V.string.minLength(10), V.string.maxLength(5000))
        addStringField("domain", V.string.nonBlank(), V.string.minLength(2), V.string.maxLength(100))
    }

    // Patent Application
    val patentSchema = V.schema("patent") {
        addStringField("title", V.string.nonBlank(), V.string.minLength(3))
        addStringField("abstract", V.string.nonBlank(), V.string.minLength(50), V.string.maxLength(10000))
        addStringField("claims", V.string.nonBlank())
    }

    // Business Registration
    val businessSchema = V.schema("business") {
        addStringField("businessName", V.string.nonBlank(), V.string.minLength(2))
        addStringField("businessType", V.string.nonBlank())
        addStringField("sector", V.string.nonBlank())
        addStringField("address", V.string.nonBlank())
        addStringField("city", V.string.nonBlank())
        addStringField("state", V.string.nonBlank())
        addStringField("pinCode", V.string.matches(Regex("^\\d{6}$"), "Must be a valid 6-digit PIN code"))
        addStringField("phone", V.string.phone())
        addStringField("panNumber", V.string.pan())
    }

    // Student Project
    val studentProjectSchema = V.schema("studentProject") {
        addStringField("studentName", V.string.nonBlank())
        addStringField("institutionName", V.string.nonBlank())
        addStringField("projectTitle", V.string.nonBlank(), V.string.minLength(5), V.string.maxLength(200))
        addStringField("projectDescription", V.string.nonBlank(), V.string.minLength(20))
        addStringField("domain", V.string.nonBlank())
    }

    // Mentor Registration
    val mentorSchema = V.schema("mentor") {
        addStringField("name", V.string.nonBlank())
        addStringField("expertise", V.string.nonBlank())
        addStringField("bio", V.string.nonBlank(), V.string.minLength(10))
    }

    // Investor Registration
    val investorSchema = V.schema("investor") {
        addStringField("name", V.string.nonBlank())
        addStringField("organizationName", V.string.nonBlank())
        addStringField("bio", V.string.nonBlank(), V.string.minLength(10))
    }

    // Grant Issuance
    val grantSchema = V.schema("grant") {
        addStringField("projectId", V.string.nonBlank())
        addStringField("grantAmount", V.string.nonBlank())
        addStringField("grantType", V.string.nonBlank())
        addStringField("releaseDate", V.string.nonBlank())
    }
}
