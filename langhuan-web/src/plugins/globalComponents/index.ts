import { App } from "vue";

const componentsFun = (require as any).context('../../components/globalComponents', true, /\index.vue$/,'sync');
let components:{} = {}
componentsFun.keys().forEach((k:string )=>{
    components[k] = componentsFun(k)
})

export default function autoRegisterComponents(app: App) {
    Object.keys(components).forEach((k) => {
        const cache = k.split("/")
        const name = k.split("/")[cache.length -2] as string;
        app.component(name, components[k].default);
    });
}
