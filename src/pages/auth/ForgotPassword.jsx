import { useState } from 'react'
import { Link } from 'react-router-dom'
import AuthButton from '../../components/auth/AuthButton'
import AuthFeedback from '../../components/auth/AuthFeedback'
import AuthInput from '../../components/auth/AuthInput'
import AuthLayout from '../../components/auth/AuthLayout'
import { validateResetEmail } from '../../auth/validation'

function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  function handleSubmit(event) {
    event.preventDefault()
    const validationError = validateResetEmail(email)
    if (validationError) {
      setError(validationError)
      setMessage('')
      return
    }

    setError('')
    setMessage('Password reset is not available in the backend yet. No reset email was sent.')
  }

  return (
    <AuthLayout
      eyebrow="Account recovery"
      title="Forgot password?"
      description="Enter the email address associated with your account. Password reset is not available yet."
    >
      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        <AuthInput
          id="reset-email"
          label="Email address"
          type="email"
          value={email}
          onChange={(event) => {
            setEmail(event.target.value)
            setError('')
            setMessage('')
          }}
          error={error}
          autoComplete="email"
          placeholder="you@mission.org"
        />
        {message ? <AuthFeedback>{message}</AuthFeedback> : null}
        <AuthButton isLoading={isLoading}>Send Reset Link</AuthButton>
      </form>

      <p className="mt-6 text-center text-sm">
        <Link to="/login" className="inline-flex min-h-11 items-center font-semibold text-cyan-200 hover:text-cyan-100">
          Back to Sign In
        </Link>
      </p>
    </AuthLayout>
  )
}

export default ForgotPassword
