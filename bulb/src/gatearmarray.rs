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
use crate::resource::{
    AncillaryData, Card, View, EDIT_BUTTON, LOC_BUTTON, NAME,
};
use crate::util::{ContainsLower, Fields, HtmlStr};
use serde::{Deserialize, Serialize};
use std::borrow::{Borrow, Cow};
use std::fmt;
use wasm_bindgen::JsValue;

/// Gate arm states
#[derive(Debug, Deserialize, Serialize)]
pub struct GateArmState {
    pub id: u32,
    pub description: String,
}

/// Gate Arm Array
#[derive(Debug, Default, Deserialize, Serialize)]
pub struct GateArmArray {
    pub name: String,
    pub location: Option<String>,
    pub notes: String,
    pub arm_state: u32,
    pub interlock: u32,
    // full attributes
    pub geo_loc: Option<String>,
}

/// Ancillary gate arm array data
#[derive(Debug, Default)]
pub struct GateArmArrayAnc {
    pub states: Option<Vec<GateArmState>>,
}

impl GateArmArrayAnc {
    /// Get state description
    fn state(&self, pri: &GateArmArray) -> &str {
        if let Some(states) = &self.states {
            for state in states {
                if pri.arm_state == state.id {
                    return &state.description;
                }
            }
        }
        ""
    }
}

const GATE_ARM_STATE_URI: &str = "/iris/gate_arm_state";

impl AncillaryData for GateArmArrayAnc {
    type Primary = GateArmArray;

    /// Get ancillary URI
    fn uri(&self, view: View, _pri: &GateArmArray) -> Option<Cow<str>> {
        match (view, &self.states) {
            (View::Search, None) => Some(GATE_ARM_STATE_URI.into()),
            _ => None,
        }
    }

    /// Put ancillary JSON data
    fn set_json(
        &mut self,
        view: View,
        pri: &GateArmArray,
        json: JsValue,
    ) -> Result<()> {
        if let Some(uri) = self.uri(view, pri) {
            match uri.borrow() {
                GATE_ARM_STATE_URI => {
                    self.states = Some(json.into_serde::<Vec<GateArmState>>()?);
                }
                _ => (),
            }
        }
        Ok(())
    }
}

impl GateArmArray {
    pub const RESOURCE_N: &'static str = "gate_arm_array";

    /// Convert to Compact HTML
    fn to_html_compact(&self, _anc: &GateArmArrayAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(12);
        format!(
            "<span>{location}</span>\
            <span class='{NAME}'>{self}</span>"
        )
    }

    /// Convert to Status HTML
    fn to_html_status(&self, _anc: &GateArmArrayAnc) -> String {
        let location = HtmlStr::new(&self.location).with_len(64);
        format!(
            "<div class='row'>\
              <span class='info'>{location}</span>\
            </div>\
            <div class='row'>\
              <span></span>\
              {LOC_BUTTON}\
              {EDIT_BUTTON}\
            </div>"
        )
    }

    /// Convert to Edit HTML
    fn to_html_edit(&self) -> String {
        format!("")
    }
}

impl fmt::Display for GateArmArray {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", HtmlStr::new(&self.name))
    }
}

impl Card for GateArmArray {
    type Ancillary = GateArmArrayAnc;

    /// Set the name
    fn with_name(mut self, name: &str) -> Self {
        self.name = name.to_string();
        self
    }

    /// Get geo location name
    fn geo_loc(&self) -> Option<&str> {
        self.geo_loc.as_deref()
    }

    /// Check if a search string matches
    fn is_match(&self, search: &str, anc: &GateArmArrayAnc) -> bool {
        self.name.contains_lower(search)
            || self.location.contains_lower(search)
            || anc.state(self).contains(search)
    }

    /// Convert to HTML view
    fn to_html(&self, view: View, anc: &GateArmArrayAnc) -> String {
        match view {
            View::Create => self.to_html_create(anc),
            View::Compact => self.to_html_compact(anc),
            View::Status => self.to_html_status(anc),
            View::Edit => self.to_html_edit(),
            _ => unreachable!(),
        }
    }

    /// Get changed fields from Edit form
    fn changed_fields(&self) -> String {
        let fields = Fields::new();
        fields.into_value().to_string()
    }
}
