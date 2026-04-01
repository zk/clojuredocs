# VS Code + Calva

Editor-specific notes for contributors using VS Code with [Calva](https://calva.io/). See [dev-setup.md](dev-setup.md) for general environment setup (MongoDB, env vars, etc.).

## Install Calva

Install from the [VS Code Marketplace](https://marketplace.visualstudio.com/items?itemName=betterthantomorrow.calva).

## Connecting to the REPL

Calva's **Jack-In** command does not source `bin/.devenv`, so the app will be missing required env vars (notably `MONGO_URL`) at startup. Instead:

1. Start the REPL manually in a terminal (see [dev-setup.md](dev-setup.md#starting-the-repl))
2. Run **Calva: Connect to a Running REPL Server** from the command palette
3. Choose **Leiningen** → enter `localhost` and the port from `.nrepl-port`

### Alternative: Configure Jack-In env vars

If you prefer Jack-In, you can copy the env vars from `bin/.devenv` into your VS Code settings so Calva provides them at startup:

```json
{
  "calva.jackInEnv": {
    "MONGO_URL": "mongodb://localhost:27017/clojuredocs",
    "ALLOW_ROBOTS": "true"
  }
}
```

See `bin/.devenv` for the full list of required variables. Copy the values from there. (Note: the credentials in `bin/.devenv` are dummy dev-only values, not production secrets — but it's still good practice to reference the source file rather than duplicating values.)

## Paredit

Calva includes Paredit for structural editing. If you're used to Emacs keybindings, you may want to add `Ctrl+K` for structural kill:

```json
// keybindings.json
{
  "key": "ctrl+k",
  "command": "paredit.killRight",
  "when": "calva:keybindingsEnabled && editorLangId =~ /clojure|clojurescript/"
}
```
