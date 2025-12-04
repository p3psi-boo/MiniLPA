<script lang="ts">
	import { Header, HeaderNav, HeaderNavItem, HeaderUtilities } from 'carbon-components-svelte';
</script>

<Header company="MiniLPA" platformName="WebUI">
	<HeaderNav>
		<HeaderNavItem href="/" text="设备" />
		<HeaderNavItem href="/profiles" text="配置文件" />
		<HeaderNavItem href="/notifications" text="通知" />
		<HeaderNavItem href="/chip" text="芯片信息" />
	</HeaderNav>
	<HeaderUtilities>
		<!-- 可以在这里添加额外的工具按钮 -->
	</HeaderUtilities>
</Header>

<style>
	:global(.bx--header) {
		position: sticky;
		top: 0;
		z-index: 1000;
	}
</style>
