import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import AuthButton from '../../components/auth/AuthButton'
import AuthFeedback from '../../components/auth/AuthFeedback'
import AuthInput from '../../components/auth/AuthInput'
import AuthLayout from '../../components/auth/AuthLayout'
import PasswordInput from '../../components/auth/PasswordInput'
import { validateLogin } from '../../auth/validation'

function Login() {
  const { login } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [values, setValues] = useState({ email: '', password: '' })
  const [errors, setErrors] = useState({})
  const [isLoading, setIsLoading] = useState(false)
  const [message, setMessage] = useState('')

  function updateValue(field, value) {
    setValues((current) => ({ ...current, [field]: value }))
    setErrors((current) => ({ ...current, [field]: '' }))
    setMessage('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    const nextErrors = validateLogin(values)
    setErrors(nextErrors)
    if (Object.keys(nextErrors).length > 0) return

    setIsLoading(true)
    setMessage('')
    setErrors({})
    try {
      const user = await login({ email: values.email.trim(), password: values.password })
      const destination = location.state?.from?.pathname
      const roleHome = user.roles.includes('ASTRONAUT')
        ? '/astronaut/dashboard'
        : user.roles.includes('MEDICAL_OFFICER') || user.roles.includes('ADMINISTRATOR')
          ? '/medical/dashboard'
          : user.roles.includes('MISSION_CONTROL')
            ? '/mission-control/dashboard'
            : '/'
      navigate(destination && destination !== '/login' ? destination : roleHome, { replace: true })
    } catch (error) {
      setIsLoading(false)
      setValues((current) => ({ ...current, password: '' }))
      setMessage(error.message || 'Sign-in failed. Please try again.')
    }
  }

  return (
    <AuthLayout
      eyebrow="Welcome back"
      title="Sign in"
      description="Sign in with your ASTRA HEALTH account. Access is determined by the roles assigned to your account."
    >
      <form className="space-y-5" onSubmit={handleSubmit} noValidate>
        <AuthInput
          id="login-email"
          label="Email address"
          type="email"
          value={values.email}
          onChange={(event) => updateValue('email', event.target.value)}
          error={errors.email}
          autoComplete="email"
          placeholder="you@mission.org"
        />
        <PasswordInput
          id="login-password"
          label="Password"
          value={values.password}
          onChange={(event) => updateValue('password', event.target.value)}
          error={errors.password}
          autoComplete="current-password"
          placeholder="Enter your password"
        />

        <div className="flex flex-wrap items-center justify-end gap-3 text-sm">
          <Link to="/forgot-password" className="inline-flex min-h-11 items-center font-medium text-cyan-200 hover:text-cyan-100">
            Forgot password?
          </Link>
        </div>

        {message ? <AuthFeedback tone="error">{message}</AuthFeedback> : null}
        <AuthButton isLoading={isLoading}>Sign In</AuthButton>
      </form>

      <p className="mt-6 text-center text-sm text-slate-400">
        New to ASTRA HEALTH?{' '}
        <Link to="/register" className="inline-flex min-h-11 items-center font-semibold text-cyan-200 hover:text-cyan-100">
          Create Account
        </Link>
      </p>
    </AuthLayout>
  )
}

export default Login
