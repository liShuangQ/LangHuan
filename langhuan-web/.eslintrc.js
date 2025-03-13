module.exports = {
    env: {
        browser: true,
        es2021: true,
        node: true,
        "vue/setup-compiler-macros": true,
    },
    parser: "vue-eslint-parser",
    extends: [
        "eslint:recommended",
        "plugin:vue/vue3-essential",
        // "standard-with-typescript",
        "plugin:@typescript-eslint/recommended",
    ],
    overrides: [],
    parserOptions: {
        ecmaVersion: 12,
        sourceType: "module",
        parser: "@typescript-eslint/parser",
    },
    plugins: ["vue", "@typescript-eslint"],
    rules: {
        indent: ["warning", 4],
        "complexity": ["error", { "max": 20 }],
        "@typescript-eslint/no-explicit-any": "off",
        "@typescript-eslint/ban-types": "off",
        quotes: "off",
        semi: "off",
        "comma-dangle": "off",
        "no-constant-condition": "off",
        "no-undef": "off",
        "prefer-const": "off",
        "vue/multi-word-component-names": "off",
        "no-async-promise-executor": "off",
        "no-debugger": "off",
        eqeqeq: "error",
        "default-case": "error",
        "no-useless-escape": "off",
        "vue/singleline-html-element-content-newline": "off",
        "vue/multiline-html-element-content-newline": "off",
        "vue/html-self-closing": "off",
        "no-unused-vars": "off",
        "vue/attributes-order": "off",
        "vue/max-attributes-per-line": "off",
        "vue/valid-v-slot": "off",
        "@typescript-eslint/no-unused-vars": "off",

        'vue/no-parsing-error': 'off',         // 禁用解析错误规则
        'vue/no-duplicate-attributes': 'off',  // 禁用重复属性规则
        'vue/no-template-shadow': 'off',         // 禁用模板作用域冲突的警告
        'vue/attribute-hyphenation': 'off',      // 禁用属性必须使用连字符的警告
        'vue/prop-name-casing': 'off',           // 禁用属性命名需使用 camelCase 的警告
        'vue/require-prop-types': 'off',         // 禁用要求 prop 定义类型的警告
    },
};
