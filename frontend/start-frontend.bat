@echo off
echo 启动前端服务...
cd /d E:\Project\frontend
set SKIP_PREFLIGHT_CHECK=true
set ESLINT_NO_DEV_ERRORS=true
npx vue-cli-service serve --skip-plugins @vue/cli-plugin-eslint
pause
