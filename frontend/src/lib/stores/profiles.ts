/**
 * 配置文件状态管理
 * 使用 Svelte 5 Runes
 */

import type { Profile, DownloadRequest } from '../api/types';
import { profileApi } from '../api/client';

class ProfileStore {
	profiles = $state<Profile[]>([]);
	loading = $state(false);
	error = $state<string | null>(null);

	/**
	 * 加载配置文件列表
	 */
	async loadProfiles() {
		this.loading = true;
		this.error = null;
		try {
			const response = await profileApi.list();
			this.profiles = response.profiles;
		} catch (err) {
			this.error = err instanceof Error ? err.message : '加载配置文件列表失败';
			console.error('Failed to load profiles:', err);
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 下载配置文件（异步，进度通过 WebSocket 推送）
	 */
	async downloadProfile(data: DownloadRequest) {
		this.loading = true;
		this.error = null;
		try {
			await profileApi.download(data);
			// 下载是异步的，完成后需要重新加载列表
			// 可以监听 WebSocket 的 complete 事件后再刷新
		} catch (err) {
			this.error = err instanceof Error ? err.message : '下载配置文件失败';
			console.error('Failed to download profile:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 启用配置文件
	 */
	async enableProfile(iccid: string) {
		this.loading = true;
		this.error = null;
		try {
			await profileApi.enable(iccid);
			await this.loadProfiles(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '启用配置文件失败';
			console.error('Failed to enable profile:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 禁用配置文件
	 */
	async disableProfile(iccid: string) {
		this.loading = true;
		this.error = null;
		try {
			await profileApi.disable(iccid);
			await this.loadProfiles(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '禁用配置文件失败';
			console.error('Failed to disable profile:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 设置配置文件昵称
	 */
	async setNickname(iccid: string, nickname: string) {
		this.loading = true;
		this.error = null;
		try {
			await profileApi.setNickname(iccid, nickname);
			await this.loadProfiles(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '设置昵称失败';
			console.error('Failed to set nickname:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 删除配置文件
	 */
	async deleteProfile(iccid: string) {
		this.loading = true;
		this.error = null;
		try {
			await profileApi.delete(iccid);
			await this.loadProfiles(); // 重新加载列表
		} catch (err) {
			this.error = err instanceof Error ? err.message : '删除配置文件失败';
			console.error('Failed to delete profile:', err);
			throw err;
		} finally {
			this.loading = false;
		}
	}

	/**
	 * 获取已启用的配置文件
	 */
	get enabledProfile(): Profile | undefined {
		return this.profiles.find((p) => p.profileState === 'enabled');
	}

	/**
	 * 清除错误
	 */
	clearError() {
		this.error = null;
	}
}

export const profileStore = new ProfileStore();
export const profilesStore = profileStore; // alias for convenience
