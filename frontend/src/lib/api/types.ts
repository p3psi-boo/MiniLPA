// API类型定义，与后端模型对应

export interface Device {
	name: string;
	path: string;
}

export interface ChipInfo {
	eid: string;
	euiccInfo2: {
		profileVersion: string;
		euiccFirmwareVer: string;
		uiccFirmwareVer: string;
		globalplatformVersion: string;
		svn: string;
		euiccCategory?: string;
		forbiddenProfilePolicyRules?: string;
		extCardResource: {
			installedApp: number;
			freeNonVolatileMemory: number;
			freeVolatileMemory: number;
		};
	};
}

export interface Profile {
	iccid: string;
	isdpAid: string;
	state: 'enabled' | 'disabled';
	profileState: 'enabled' | 'disabled'; // alias for backward compatibility
	nickname?: string;
	profileNickname?: string; // alias for backward compatibility
	serviceProviderName: string;
	profileName: string;
	iconType?: string;
	icon?: string;
	profileClass: 'operational' | 'provisioning';
}

export interface Notification {
	seqNumber: number;
	profileManagementOperation: 'install' | 'enable' | 'disable' | 'delete';
	notificationAddress: string;
	iccid?: string;
}

export interface DownloadRequest {
	activationCode?: string;
	smdp?: string;
	matchingId?: string;
	confirmationCode?: string;
	imei?: string;
}

export interface NicknameRequest {
	nickname: string;
}

export interface QRCodeParseResponse {
	activationCode: string;
	smdp: string;
	matchingId?: string;
	oid?: string;
	confirmationCode?: string;
	imei?: string;
}

export interface ProgressMessage {
	type: 'progress' | 'complete' | 'error' | 'info';
	message: string;
	percent?: number;
}

export interface ApiError {
	error: string;
}

export interface ApiResponse<T = unknown> {
	data?: T;
	error?: string;
}
