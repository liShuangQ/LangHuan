import { App } from "vue";
import VMdPreview from "@kangc/v-md-editor/lib/preview";
import "@kangc/v-md-editor/lib/style/preview.css";
import githubTheme from "@kangc/v-md-editor/lib/theme/github.js";
import "@kangc/v-md-editor/lib/theme/style/github.css";
import VueMarkdownEditor from '@kangc/v-md-editor';
import '@kangc/v-md-editor/lib/style/base-editor.css';
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js';
import hljs from 'highlight.js';
// VMdPreview.use(githubTheme);

VMdPreview.use(githubTheme, {
    Hljs: hljs,
  });
export default function setMdDditor(app: App) {
    app.use(VMdPreview);
}
