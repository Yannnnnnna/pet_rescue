import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import uviewPlus from 'uview-plus';
// 引入 uView-Plus 样式
import 'uview-plus/index.scss';

export function createApp() {
	const app = createSSRApp(App);
	// 使用 uView-Plus
	app.use(uviewPlus);
	return {
		app,
	};
}
