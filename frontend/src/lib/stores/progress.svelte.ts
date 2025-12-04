/**
 * 进度状态管理
 * 使用 Svelte 5 Runes
 */

import type { ProgressMessage } from '../api/types';
import { getProgressWebSocket } from '../api/websocket';

class ProgressStore {
	currentOperation = $state<string | null>(null);
	message = $state<string>('');
	percent = $state<number>(0);
	isOperating = $state(false);
	type = $state<'progress' | 'complete' | 'error' | 'info'>('info');

	private ws = getProgressWebSocket();
	private unsubscribe: (() => void) | null = null;

	/**
	 * 初始化 WebSocket 连接
	 */
	init() {
		// 连接 WebSocket
		this.ws.connect();

		// 订阅进度消息
		this.unsubscribe = this.ws.subscribe((message: ProgressMessage) => {
			this.handleProgressMessage(message);
		});
	}

	/**
	 * 处理进度消息
	 */
	private handleProgressMessage(message: ProgressMessage) {
		this.type = message.type;
		this.message = message.message;

		switch (message.type) {
			case 'progress':
				this.isOperating = true;
				this.percent = message.percent ?? 0;
				break;

			case 'complete':
				this.isOperating = false;
				this.percent = 100;
				// 3秒后清除消息
				setTimeout(() => {
					this.reset();
				}, 3000);
				break;

			case 'error':
				this.isOperating = false;
				this.percent = 0;
				// 5秒后清除错误消息
				setTimeout(() => {
					this.reset();
				}, 5000);
				break;

			case 'info':
				// 信息消息，不改变操作状态
				break;
		}
	}

	/**
	 * 开始操作
	 */
	startOperation(operation: string) {
		this.currentOperation = operation;
		this.isOperating = true;
		this.message = `正在${operation}...`;
		this.percent = 0;
		this.type = 'progress';
	}

	/**
	 * 重置状态
	 */
	reset() {
		this.currentOperation = null;
		this.message = '';
		this.percent = 0;
		this.isOperating = false;
		this.type = 'info';
	}

	/**
	 * 清理资源
	 */
	destroy() {
		if (this.unsubscribe) {
			this.unsubscribe();
			this.unsubscribe = null;
		}
		this.ws.disconnect();
	}
}

export const progressStore = new ProgressStore();
