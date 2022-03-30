// Copyright (C) 2022  Minnesota Department of Transportation
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
use crate::error::Result;
use crate::resource::{disabled_attr, AncillaryData, Card, View};
use crate::util::{ContainsLower, Dom};
use serde::{Deserialize, Serialize};
use serde_json::map::Map;
use serde_json::Value;
use std::fmt;
use wasm_bindgen::JsValue;
use web_sys::Document;

/// Role
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct Role {
    pub name: String,
    pub enabled: bool,
}

/// Ancillary role data
#[derive(Debug, Default)]
pub struct RoleAnc;

impl AncillaryData for RoleAnc {
    type Primary = Role;
}

impl Role {
    pub const RESOURCE_N: &'static str = "role";

    /// Convert to Compact HTML
    fn to_html_compact(&self) -> String {
        let disabled = disabled_attr(self.enabled);
        format!("<span{disabled}>{self}</span>")
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        let enabled = if self.enabled { " checked" } else { "" };
        format!(
            "<div class='row'>\
              <label for='edit_enabled'>Enabled</label>\
              <input id='edit_enabled' type='checkbox'{enabled}/>\
            </div>"
        )
    }
}

impl fmt::Display for Role {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.name)
    }
}

impl Card for Role {
    type Ancillary = RoleAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, _anc: &RoleAnc) -> bool {
        self.name.contains_lower(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &RoleAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(doc: &Document, json: &JsValue) -> Result<String> {
        let val = Self::new(json)?;
        let mut obj = Map::new();
        if let Some(enabled) = doc.input_bool("edit_enabled") {
            if enabled != val.enabled {
                obj.insert("enabled".to_string(), Value::Bool(enabled));
            }
        }
        Ok(Value::Object(obj).to_string())
    }
}
