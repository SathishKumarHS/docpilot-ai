const PREFIX = "[docpilot]"

export function logError(context: string, error: unknown, extra?: Record<string, unknown>) {
  console.error(PREFIX, context, error, extra ?? "")
}

export function logWarn(context: string, extra?: Record<string, unknown>) {
  console.warn(PREFIX, context, extra ?? "")
}

export function logInfo(context: string, extra?: Record<string, unknown>) {
  console.info(PREFIX, context, extra ?? "")
}
