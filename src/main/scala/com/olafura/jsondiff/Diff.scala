package com.olafura.jsondiff

import play.api.libs.json._

class JsonDiff(oldJson: JsValue, newJson: JsValue):
  def diff(): JsValue = JsNull
