function AuthInput({
  id,
  label,
  type = 'text',
  value,
  onChange,
  error,
  autoComplete,
  placeholder,
  required = true,
  trailing,
}) {
  const errorId = error ? `${id}-error` : undefined

  return (
    <div className="min-w-0">
      <label htmlFor={id} className="mb-2 block text-sm font-medium text-slate-200">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          name={id}
          type={type}
          value={value}
          onChange={onChange}
          autoComplete={autoComplete}
          placeholder={placeholder}
          required={required}
          aria-invalid={Boolean(error)}
          aria-describedby={errorId}
          className={`min-h-12 w-full rounded-xl border bg-slate-950/70 px-4 py-3 text-base text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-300/70 focus:ring-2 focus:ring-cyan-300/15 ${
            error ? 'border-rose-400/70' : 'border-white/10'
          } ${trailing ? 'pr-12' : ''}`}
        />
        {trailing ? <div className="absolute inset-y-0 right-1 flex items-center">{trailing}</div> : null}
      </div>
      {error ? (
        <p id={errorId} className="mt-1.5 text-xs text-rose-300" role="alert">
          {error}
        </p>
      ) : null}
    </div>
  )
}

export default AuthInput
