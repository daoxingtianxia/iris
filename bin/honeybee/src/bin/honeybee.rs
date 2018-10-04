/*
 * Copyright (C) 2018  Minnesota Department of Transportation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
extern crate honeybee;
extern crate users;

use std::env;
use honeybee::fetcher;
use users::get_current_username;

fn main() {
    let host = env::args().nth(1);
    let username = get_current_username().expect("User name lookup error");
    fetcher::start(username, host).unwrap();
}
