import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
	plugins: [sveltekit()],
	resolve: {
		extensions: ['.js', '.ts', '.svelte', '.svelte.js', '.svelte.ts'],
	},
	server: {
		proxy: {
			// API 代理到后端
			'/api': {
				target: 'http://127.0.0.1:8080',
				changeOrigin: true,
			},
			// WebSocket 代理到后端
			'/ws': {
				target: 'ws://127.0.0.1:8080',
				ws: true,
			},
		},
	},
	optimizeDeps: {
		include: ['carbon-components-svelte', 'carbon-icons-svelte'],
	},
});
