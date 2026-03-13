/**
 * ---------------------------------------------------------------------------
 *  environment.codespace.ts
 * ---------------------------------------------------------------------------
 * Purpose
 *  - This environment file is used when running the Angular front end
 *    inside GitHub Codespaces so that all API calls target the TAF
 *    API Gateway exposed via the Codespace’s forwarded port (HTTPS).
 *
 * Why this file
 *  - In Codespaces, each forwarded port gets a public URL like:
 *      https://<codespace-name>-<port>.preview.app.github.dev
 *    Setting apiUrl here avoids hard-coding URLs in services and keeps
 *    local/dev/prod configurations clean and separate.
 *
 * When to use it
 *  - Use this file whenever you run `ng serve` or `ng build` inside a
 *    Codespace and want the UI to call the backend through the Gateway
 *    (port 8080) via the Codespace URL.
 *  - Your Angular `angular.json` will map environment.ts -> environment.codespace.ts
 *    using "fileReplacements" for the "serve" and/or "build" targets.
 *
 * How to update
 *  - Replace the placeholder below with YOUR Codespace gateway URL:
 *      https://<your-codespace>-8080.preview.app.github.dev
 *  - If your Codespace name or the forwarded port changes, update this
 *    value and restart `ng serve`.
 *
 * Security / CORS
 *  - Because the UI (port 4200) and Gateway (port 8080) use different
 *    subdomains, ensure the Gateway CORS config allows the UI origin
 *    (the -4200 URL). If you see CORS errors in the browser console,
 *    update the Gateway’s allowed origins accordingly.
 *
 * Notes
 *  - This file is for development in Codespaces only; do not commit
 *    credentials or secrets here. For production builds, continue to use
 *    environment.prod.ts with the proper API base URL.
 * ---------------------------------------------------------------------------
 */

export const environment = {
  production: false,

apiUrl: '/api'
};