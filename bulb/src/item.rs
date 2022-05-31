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
use std::fmt;

/// Item state
#[derive(Clone, Copy)]
pub enum ItemState {
    Inactive,
    Available,
    Deployed,
    Maintenance,
}

impl ItemState {
    /// Get the item state code
    pub fn code(self) -> &'static str {
        match self {
            Self::Inactive => "❓",
            Self::Available => "🔹",
            Self::Deployed => "🔶",
            Self::Maintenance => "◼️",
        }
    }

    /// Get the item state description
    pub fn description(self) -> &'static str {
        match self {
            Self::Inactive => "inactive",
            Self::Available => "available",
            Self::Deployed => "deployed",
            Self::Maintenance => "maintenance",
        }
    }
}

impl fmt::Display for ItemState {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{}", self.code())
    }
}
