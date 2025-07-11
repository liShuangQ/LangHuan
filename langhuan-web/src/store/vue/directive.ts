import dayjs from 'dayjs'
import { App } from 'vue';
 
const setDirective =(app:App)=> {
    app.directive('format-time', {
        mounted (el, binding) {
            const time = el.textContent * 1
            el.textContent = dayjs(time).format(binding.value || 'YYYY-MM-DD HH:mm:ss')
        }
    })
}
export default setDirective