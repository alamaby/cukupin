package com.alamaby.cukupin.domain.time

import java.time.LocalDate

class SystemDateProvider : DateProvider {
    override fun today(): LocalDate = LocalDate.now()
}
