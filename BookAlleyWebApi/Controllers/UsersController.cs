using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BCrypt.Net;
using BookAlleyWebApi.Models;
using BookAlleyWebApi.RestModels;

namespace BookAlleyWebApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class UsersController : ControllerBase
    {
        private readonly BookAlleyContext _context;

        public UsersController(BookAlleyContext context)
        {
            _context = context;
        }

        // POST: api/Users/5
        [HttpPost("signin")]
        public async Task<ActionResult> SignIn([FromBody] SignIn user)
        {
            if (_context.Users == null)
            {
                return NotFound();
            }
            var userFromDB = await _context.Users.FirstOrDefaultAsync(u => u.Email == user.Email);
            if (userFromDB == null)
            {
                return NotFound();
            } else if (!BCrypt.Net.BCrypt.Verify(user.Password, userFromDB.Password))
            {
                return NotFound();
            }

            //Create and return a new session token. Client must store this session token to  be used in future requests.
            SessionToken sessionToken = new() { User = userFromDB, Id = Guid.NewGuid() };
            _context.SessionTokens.Add(sessionToken);
            _context.SaveChanges();

            return Ok(new { sessionToken = sessionToken.Id });
        }
        [HttpPost("signout")]

        public async Task<ActionResult> SignOut(string sessionToken)
        {
            if (_context.SessionTokens == null)
            {
                return NotFound();
            }
            //Check and delete the session token from DB. The client should signout regardless of the result of this check.
            var sessionTokenFromDB = await _context.SessionTokens.FirstOrDefaultAsync(st => st.Id.ToString() == sessionToken);
            if (sessionTokenFromDB == null)
            {
                return NotFound();
            }
            _context.SessionTokens.Remove(sessionTokenFromDB);
            _context.SaveChanges();
            return Ok(new { message = "Sign out successful"});
        }

        // POST: api/Users
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult> SignUp(SignUp user)
        {
            if (_context.Users == null)
            {
                return Problem("Entity set 'UserContext.Users'  is null.");
            }

            //Check if email is already in use
            if (UserExists(user.Email))
            {
                return Conflict("Email already in use.");
            }
            string salt = BCrypt.Net.BCrypt.GenerateSalt();
            string hashedPassword = BCrypt.Net.BCrypt.HashPassword(user.Password, salt);

            var newUser = new User { Name = user.Name, Email = user.Email, Password = hashedPassword };
            _context.Users.Add(newUser);
            await _context.SaveChangesAsync();

            return CreatedAtAction("SignUp", new { message = "Sign up successful" });
        }

        private bool UserExists(string email)
        {
            return (_context.Users?.Any(e => e.Email == email)).GetValueOrDefault();
        }
    }
}
