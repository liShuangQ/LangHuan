module.exports = {
    presets: [
        "@babel/preset-env",
        [
            "@babel/preset-typescript",
            {
                allExtensions: true,
            },
        ],
    ],
    "plugins": ["@vue/babel-plugin-jsx"]
    // plugins: ["@babel/plugin-syntax-dynamic-import"],
};
