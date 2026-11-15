export function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())
}

function getEmailError(value) {
  const email = value.trim()
  if (!email) return 'Enter your email address.'
  if (!isValidEmail(email)) return 'Enter a valid email address.'
  return ''
}

export function validateLogin(values) {
  const errors = {}
  const emailError = getEmailError(values.email)

  if (emailError) errors.email = emailError
  if (!values.password) errors.password = 'Enter your password.'

  return errors
}

export function validateRegistration(values) {
  const errors = {}
  const emailError = getEmailError(values.email)

  if (values.fullName.trim().length < 2) errors.fullName = 'Enter your full name.'
  if (emailError) errors.email = emailError
  if (values.password.length < 12) errors.password = 'Use a password with at least 12 characters.'
  if (!values.confirmPassword) errors.confirmPassword = 'Confirm your password.'
  else if (values.password !== values.confirmPassword) errors.confirmPassword = 'Passwords do not match.'
  if (!values.acceptedTerms) errors.acceptedTerms = 'Accept the Terms and Conditions to continue.'

  return errors
}

export function validateResetEmail(email) {
  return getEmailError(email)
}
