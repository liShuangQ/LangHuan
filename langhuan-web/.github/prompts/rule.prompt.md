# 使用vue3+ts，写vue3时要使用setup语法
# ui组件库为elementplus，同时也可以使用tailwindcss
# 接口请求的封装方法为下面方法，
```js
        const res = await http.request<any>({
            url: '接口地址',
            method: 'post',   // post｜get
            q_throttle: false, // 自定义 是否开启节流
            q_spinning: true,  // 自定义 是否出现旋转蒙层
            /**
                * 影响请求头中的 Content-Type
                * form: application/x-www-form-urlencoded
                * json: application/json
                * 枚举为 "form" | "json"
            */
            q_contentType: 'json',
            data: {
                id: '1',
            }
        })
```