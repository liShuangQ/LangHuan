package com.langhuan.model.pojo

import cn.hutool.core.lang.Assert
import lombok.extern.slf4j.Slf4j
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Slf4j
class AccountUser : UserDetails {
    companion object {
        private const val serialVersionUID = -1L
    }
    
    private var userId: Integer? = null
    private var password: String? = null
    private var username: String? = null
    private var authorities: MutableCollection<out GrantedAuthority>? = null
    private var accountNonExpired: Boolean = false
    private var accountNonLocked: Boolean = false
    private var credentialsNonExpired: Boolean = false
    private var enabled: Boolean = false

    constructor(userId: Integer?, username: String?, password: String?, authorities: MutableCollection<out GrantedAuthority>?) : this(userId, username, password, true, true, true, true, authorities)

    constructor(userId: Integer?, username: String?, password: String?, enabled: Boolean, accountNonExpired: Boolean, credentialsNonExpired: Boolean, accountNonLocked: Boolean, authorities: MutableCollection<out GrantedAuthority>?) {
        Assert.isTrue(username != null && "" != username && password != null, "Cannot pass null or empty values to constructor")
        this.userId = userId
        this.username = username
        this.password = password
        this.enabled = enabled
        this.accountNonExpired = accountNonExpired
        this.credentialsNonExpired = credentialsNonExpired
        this.accountNonLocked = accountNonLocked
        this.authorities = authorities
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return this.authorities ?: mutableListOf()
    }

    fun getUserId(): Integer? {
        return this.userId
    }

    override fun getPassword(): String? {
        return this.password
    }

    override fun getUsername(): String? {
        return this.username
    }

    override fun isAccountNonExpired(): Boolean {
        return this.accountNonExpired
    }

    override fun isAccountNonLocked(): Boolean {
        return this.accountNonLocked
    }

    override fun isCredentialsNonExpired(): Boolean {
        return this.credentialsNonExpired
    }

    override fun isEnabled(): Boolean {
        return this.enabled
    }
}
