import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	preprocess: vitePreprocess(),

	kit: {
		// 使用 static adapter 将前端打包为静态文件
		adapter: adapter({
			// 输出到后端的 resources/static 目录
			pages: '../backend/src/main/resources/static',
			assets: '../backend/src/main/resources/static',
			fallback: 'index.html',
			precompress: false,
			strict: true
		})
	}
};

export default config;
