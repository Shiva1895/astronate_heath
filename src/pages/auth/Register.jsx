import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import { PUBLIC_REGISTRATION_ROLE, ROLE_LABELS } from '../../auth/roles'
import AuthButton from '../../components/auth/AuthButton'
import AuthFeedback from '../../components/auth/AuthFeedback'
import AuthInput from '../../components/auth/AuthInput'
import AuthLayout from '../../components/auth/AuthLayout'
import PasswordInput from '../../components/auth/PasswordInput'
import { validateRegistration } from '../../auth/validation'

function Register() {
  const { register } = useAuth()
  const [values, setValues] = useState({
    fullName: '',
    email: '',
    password: '',
    confirmPassword: '',
    acceptedTerms: false,
  })
  const [errors, setErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')

  function updateValue(field, value) {
    const nextValues = { ...values, [field]: value }
    setValues(nextValues)
    setErrors((current) => {
      const nextErrors = { ...current, [field]: '' }
      delete nextErrors.form
      if (
        (field === 'password' || field === 'confirmPassword') &&
        (!nextValues.confirmPassword || nextValues.password === nextValues.confirmPassword)
      ) {
        nextErrors.confirmPassword = ''
      }
      return nextErrors
    })
    setMessage('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const nextErrors = validateRegistration(values)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    setMessage('')
    setErrors({})
    try {
      const result = await register({
        email: values.email.trim(),
        displayName: values.fullName.trim(),
        password: values.password,
      })
      setIsLoading(false)
      setValues((current) => ({ ...current, password: '', confirmPassword: '' }))
      setMessage(
        `Registration received for ${result.email}. Your account is ${result.status}; an administrator must activate it before you can sign in. Public access is limited to ${ROLE_LABELS[PUBLIC_REGISTRATION_ROLE]}.`,
      )
    } catch (error) {
      setIsLoading(false)
      setValues((current) => ({ ...current, password: '', confirmPassword: '' }))
      setMessage(error.message || 'Registration failed. Please try again.')
      setErrors({ form: true })
    }
  }

  return (
    <AuthLayout
      eyebrow="Join the mission"
      title="Create account"
      description="Create an account. Public registrations request astronaut access and require administrator activation."
    >
      <form className="space-y-4" onSubmit={handleSubmit} noValidate>
        <AuthInput
          id="register-full-name"
          label="Full name"
          value={values.fullName}
          onChange={(event) => updateValue('fullName', event.target.value)}
          error={errors.fullName}
          autoComplete="name"
          placeholder="Your full name"
        />
        <AuthInput
          id="register-email"
          label="Email address"
          type="email"
          value={values.email}
          onChange={(event) => updateValue('email', event.target.value)}
          error={errors.email}
          autoComplete="email"
          placeholder="you@mission.org"
        />
        <PasswordInput
          id="register-password"
          label="Password"
          value={values.password}
          onChange={(event) => updateValue('password', event.target.value)}
          error={errors.password}
          autoComplete="new-password"
          placeholder="At least 12 characters"
        />
        <PasswordInput
          id="register-confirm-password"
          label="Confirm password"
          value={values.confirmPassword}
          onChange={(event) => updateValue('confirmPassword', event.target.value)}
          error={errors.confirmPassword}
          autoComplete="new-password"
          placeholder="Re-enter your password"
        />

        <div className="space-y-2">
          <label className="flex min-h-11 cursor-pointer items-start gap-3 text-sm leading-6 text-slate-300">
            <input
              type="checkbox"
              checked={values.acceptedTerms}
              onChange={(event) => updateValue('acceptedTerms', event.target.checked)}
              aria-invalid={Boolean(errors.acceptedTerms)}
              aria-describedby={errors.acceptedTerms ? 'register-terms-error' : undefined}
              className="mt-1 h-4 w-4 shrink-0 accent-cyan-400"
            />
            <span>I agree to the Terms and Conditions.</span>
          </label>
          {errors.acceptedTerms ? (
            <p id="register-terms-error" className="text-xs text-rose-300" role="alert">
              {errors.acceptedTerms}
            </p>
          ) : null}
        </div>

        <p className="rounded-lg border border-cyan-300/15 bg-cyan-400/[0.05] px-3 py-2 text-xs leading-5 text-cyan-100/85">
          Public registration requests {ROLE_LABELS[PUBLIC_REGISTRATION_ROLE]} access. Medical Officer, Mission Control,
          and Administrator roles are never selected in this form and must be assigned by the backend after approval.
        </p>
        {message ? <AuthFeedback tone={errors.form ? 'error' : 'success'}>{message}</AuthFeedback> : null}
        <AuthButton isLoading={isLoading}>Create Account</AuthButton>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        Already have an account?{' '}
        <Link to="/login" className="inline-flex min-h-11 items-center font-semibold text-cyan-200 hover:text-cyan-100">
          Sign In
        </Link>
      </p>
    </AuthLayout>
  )
}

export default Register
