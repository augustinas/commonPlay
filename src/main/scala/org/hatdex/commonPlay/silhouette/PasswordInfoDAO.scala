/*
 * Copyright (C) 2016 HAT Data Exchange Ltd - All Rights Reserved
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * Written by Andrius Aucinas <andrius.aucinas@hatdex.org>, 10 2016
 */

package org.hatdex.commonPlay.silhouette

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import org.hatdex.commonPlay.services.UserService
import org.hatdex.commonPlay.silhouette.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PasswordInfoDAO @Inject() (userService: UserService) extends DelegableAuthInfoDAO[PasswordInfo] {

  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    update(loginInfo, authInfo)

  def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    userService.findByEmail(loginInfo).map {
      case Some(user) if user.emailConfirmed => Some(user.password)
      case _                                 => None
    }

  def remove(loginInfo: LoginInfo): Future[Unit] = userService.remove(loginInfo)

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None    => add(loginInfo, authInfo)
    }

  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    userService.findByEmail(loginInfo).map {
      case Some(user) => {
        userService.save(user.copy(password = authInfo))
        authInfo
      }
      case _ => throw new Exception("PasswordInfoDAO - update : the user must exists to update its password")
    }

}