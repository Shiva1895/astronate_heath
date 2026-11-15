import { Eye, EyeOff } from 'lucide-react'
import { useState } from 'react'
import AuthInput from './AuthInput'

function PasswordInput(props) {
  const [visible, setVisible] = useState(false)

  return (
    <AuthInput
      {...props}
      type={visible ? 'text' : 'password'}
      trailing={
        <button
          type="button"
          onClick={() => setVisible((current) => !current)}
          className="flex h-11 w-11 items-center justify-center rounded-lg text-slate-400 transition hover:text-cyan-200 focus-visible:outline-2 focus-visible:outline-cyan-300"
          aria-label={visible ? 'Hide password' : 'Show password'}
          aria-pressed={visible}
        >
          {visible ? <EyeOff size={17} /> : <Eye size={17} />}
        </button>
      }
    />
  )
}

export default PasswordInput
