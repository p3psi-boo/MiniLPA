/**
 * WebSocket 客户端封装
 * 用于接收后端的实时进度推送
 */

import type { ProgressMessage } from './types';

export type ProgressCallback = (message: ProgressMessage) => void;

export class ProgressWebSocket {
	private ws: WebSocket | null = null;
	private callbacks: Set<ProgressCallback> = new Set();
	private reconnectAttempts = 0;
	private maxReconnectAttempts = 5;
	private reconnectDelay = 1000; // 1秒
	private shouldReconnect = true;
	private url: string;

	constructor(url = '/ws/progress') {
		// 根据当前协议判断使用 ws 或 wss
		const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
		const host = window.location.host;
		this.url = `${protocol}//${host}${url}`;
	}

	/**
	 * 连接 WebSocket
	 */
	connect(): void {
		if (this.ws?.readyState === WebSocket.OPEN) {
			console.log('WebSocket already connected');
			return;
		}

		try {
			this.ws = new WebSocket(this.url);

			this.ws.onopen = () => {
				console.log('WebSocket connected');
				this.reconnectAttempts = 0;
			};

			this.ws.onmessage = (event) => {
				try {
					const message: ProgressMessage = JSON.parse(event.data);
					this.callbacks.forEach((callback) => callback(message));
				} catch (error) {
					console.error('Failed to parse WebSocket message:', error);
				}
			};

			this.ws.onerror = (error) => {
				console.error('WebSocket error:', error);
			};

			this.ws.onclose = () => {
				console.log('WebSocket disconnected');
				this.ws = null;

				// 自动重连
				if (this.shouldReconnect && this.reconnectAttempts < this.maxReconnectAttempts) {
					this.reconnectAttempts++;
					console.log(
						`Reconnecting... (attempt ${this.reconnectAttempts}/${this.maxReconnectAttempts})`
					);
					setTimeout(() => this.connect(), this.reconnectDelay * this.reconnectAttempts);
				} else if (this.reconnectAttempts >= this.maxReconnectAttempts) {
					console.error('Max reconnect attempts reached');
				}
			};
		} catch (error) {
			console.error('Failed to create WebSocket:', error);
		}
	}

	/**
	 * 断开 WebSocket
	 */
	disconnect(): void {
		this.shouldReconnect = false;
		if (this.ws) {
			this.ws.close();
			this.ws = null;
		}
	}

	/**
	 * 订阅进度消息
	 */
	subscribe(callback: ProgressCallback): () => void {
		this.callbacks.add(callback);
		// 返回取消订阅函数
		return () => {
			this.callbacks.delete(callback);
		};
	}

	/**
	 * 获取连接状态
	 */
	get connected(): boolean {
		return this.ws?.readyState === WebSocket.OPEN;
	}
}

// 全局单例
let progressWS: ProgressWebSocket | null = null;

/**
 * 获取 ProgressWebSocket 单例
 */
export function getProgressWebSocket(): ProgressWebSocket {
	if (!progressWS) {
		progressWS = new ProgressWebSocket();
	}
	return progressWS;
}
