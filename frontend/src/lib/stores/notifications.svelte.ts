/**
 * 通知状态管理
 * 使用 Svelte 5 Runes
 */

import type { Notification } from '../api/types';
import { notificationApi } from '../api/client';

class NotificationStore {
	notifications = $state<Notification[]>([]);
	loading = $state(false);
	error = $state<string | null>(null);

	/**
	 * 加载通知列表
	 */
	async loadNotifications() {
		this.loading = true;
		this.error = null;
		try {
			const response = await notificationApi.list();
			this.notifications = response.notifications;
		} catch (err) {
			this.error = err instanceof Error ? err.message : '加载通知列表失败';
			console.error('Failed to load notifications:', err);
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 处理通知
	 */
	async processNotification(seqNumber: number) {
		this.loading = true;
		this.error = null;
		try {
			await notificationApi.process(seqNumber);
			await this.loadNotifications(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '处理通知失败';
			console.error('Failed to process notification:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 删除通知
	 */
	async deleteNotification(seqNumber: number) {
		this.loading = true;
		this.error = null;
		try {
			await notificationApi.delete(seqNumber);
			await this.loadNotifications(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '删除通知失败';
			console.error('Failed to delete notification:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 清除错误
	 */
	clearError() {
		this.error = null;
	}
}

export const notificationStore = new NotificationStore();
export const notificationsStore = notificationStore; // alias for convenience
