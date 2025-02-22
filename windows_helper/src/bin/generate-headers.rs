use std::io;
use windows_helper::generate_headers;

fn main() -> io::Result<()> {
    generate_headers()
}