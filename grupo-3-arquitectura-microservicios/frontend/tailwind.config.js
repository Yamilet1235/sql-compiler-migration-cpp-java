/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        "background": "#0b1326",
        "surface": "#0b1326",
        "primary": "#adc6ff",
        "secondary": "#d0bcff",
        "tertiary": "#4edea3",
        "on-surface": "#dae2fd",
        "outline-variant": "#424754",
        "surface-container-low": "#131b2e",
        "primary-container": "#4d8eff",
        "on-primary-container": "#00285d",
      },
    },
  },
  plugins: [],
}