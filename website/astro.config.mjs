import { defineConfig } from 'astro/config';

export default defineConfig({
  site: 'https://daniel-kindl.github.io',
  base: '/ocho',
  output: 'static',
  trailingSlash: 'never',
  build: {
    format: 'file',
  },
});
