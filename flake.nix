{
  description = "MiniLPA WebUI - 开发环境";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = true;
        };

        # Java 21 (项目要求)
        jdk = pkgs.jdk21;

        # Gradle 包装
        gradle = pkgs.gradle;

        # Node.js (用于 Svelte 前端)
        nodejs = pkgs.nodejs_22;

        # 开发工具
        devTools = with pkgs; [
          # 构建工具
          jdk
          gradle
          nodejs
          nodePackages.pnpm  # 或者使用 npm/yarn

          # lpac - eSIM 管理工具
          lpac

          # 代码格式化
          nodePackages.prettier
          ktlint

          # 调试和测试工具
          curl
          jq
          websocat  # WebSocket 测试

          # Git 工具
          git
          gh  # GitHub CLI (可选)

          # 编辑器/IDE 支持
          nodePackages.typescript-language-server
          kotlin-language-server
        ];

      in
      {
        # 开发环境
        devShells.default = pkgs.mkShell {
          buildInputs = devTools;

          shellHook = ''
            echo "🚀 MiniLPA WebUI 开发环境已就绪"
            echo ""
            echo "📦 已安装的工具："
            echo "  • Java:    $(java -version 2>&1 | head -n 1)"
            echo "  • Gradle:  $(gradle --version | grep Gradle | head -n 1)"
            echo "  • Node.js: $(node --version)"
            echo "  • pnpm:    $(pnpm --version)"
            echo ""
            echo "📂 项目结构："
            echo "  • backend/  - Ktor Server (Kotlin)"
            echo "  • frontend/ - Svelte App (TypeScript)"
            echo ""
            echo "🔧 常用命令："
            echo "  • cd backend && gradle run       - 启动后端服务器"
            echo "  • cd frontend && pnpm dev        - 启动前端开发服务器"
            echo "  • cd backend && gradle build     - 构建后端"
            echo "  • cd frontend && pnpm build      - 构建前端"
            echo ""

            # 设置 Java 环境变量
            export JAVA_HOME="${jdk}"
            export PATH="$JAVA_HOME/bin:$PATH"

            # Gradle 配置
            export GRADLE_USER_HOME="$PWD/.gradle"

            # Node.js 配置
            export NODE_ENV="development"
          '';

          # 环境变量
          JAVA_HOME = "${jdk}";
          GRADLE_OPTS = "-Dorg.gradle.daemon=true";
        };

        # 可选：生产构建环境
        packages.default = pkgs.stdenv.mkDerivation {
          pname = "minilpa-web";
          version = "2.0.0";
          src = ./.;

          buildInputs = [ jdk gradle nodejs ];

          buildPhase = ''
            # 构建后端
            cd backend
            gradle build --no-daemon
            cd ..

            # 构建前端
            cd frontend
            pnpm install
            pnpm build
            cd ..
          '';

          installPhase = ''
            mkdir -p $out
            cp -r backend/build/libs/* $out/
            cp -r frontend/build/* $out/static/
          '';
        };
      }
    );
}
