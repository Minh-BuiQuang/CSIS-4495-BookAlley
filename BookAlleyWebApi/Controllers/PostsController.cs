using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BookAlleyWebApi.Models;
using BookAlleyWebApi.RestModels;
using Microsoft.Build.Framework;

namespace BookAlleyWebApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class PostsController : ControllerBase
    {
        private readonly BookAlleyContext _context;

        public PostsController(BookAlleyContext context)
        {
            _context = context;
        }

        // GET: api/Posts
        [HttpGet]
        public async Task<ActionResult<IEnumerable<PostResponse>>> GetPosts([FromQuery]Guid? SessionToken, [FromQuery] bool isStatisticRequest)
        {
            List<Post> posts = new();

            if (_context.Posts == null)
            {
                return NotFound();
            }
            if (SessionToken != null)
            {
                //Get post by user. Used when user wants to see their own posts

                var token = await _context.SessionTokens.Include(s => s.User).FirstOrDefaultAsync(s => s.Id == SessionToken);
                var user = token?.User;
                if (user == null)
                {
                    return Problem("User not found, please login");
                }
                posts = await _context.Posts.Where(p => p.Poster.Id == user.Id).Include(p => p.Poster).ToListAsync();
            }
            else 
            {
                if(isStatisticRequest)
                    posts = await _context.Posts.Include(p => p.Poster).ToListAsync();
                else
                    posts = await _context.Posts.Include(p => p.Poster).Where(p => p.IsDeleted != true).ToListAsync();
            }
            var results = posts.Select(x => new PostResponse()
            {
                Id = x.Id,
                PosterName = x.Poster.Name,
                PosterId = x.Poster.Id,
                BookTitle = x.BookTitle,
                Author = x.Author,
                ISBN = x.ISBN,
                Image = x.Image,
                Location = x.Location,
                Note = x.Note,
                DatePosted = x.DatePosted
            }).ToList();
            return results;
        }

        // POST: api/Posts
        // To protect from overposting attacks, see https://go.microsoft.com/fwlink/?linkid=2123754
        [HttpPost]
        public async Task<ActionResult> PostPost([FromQuery] Guid sessionToken, [FromBody] CreatePostRequest body)
        {

            if (_context.Posts == null)
            {
                return Problem("Entity set 'BookAlleyContext.Posts'  is null.");
            }
            //Get user from session token
            var token = await _context.SessionTokens.Include(s => s.User).FirstOrDefaultAsync(s => s.Id == sessionToken);
            var user = token?.User;
            if (user == null)
            {
                return Problem("User not found, please login");
            }
            //Create post
            var post = new Post
            {
                Poster = user,
                Note = body.Note,
                Author = body.Author,
                BookTitle = body.BookTitle, 
                ISBN = body.ISBN,
                Image = body.Image,
                Location = body.Location,
                DatePosted = DateTime.Now
            };
            _context.Posts.Add(post);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Post created" });
        }

        // DELETE: api/Posts/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeletePost([FromQuery] Guid sessionToken, long id)
        {
            if (_context.Posts == null)
            {
                return NotFound();
            }
            var post = await _context.Posts.FindAsync(id);
            if (post == null)
            {
                return NotFound();
            }
            //Get user from session token
            var token = await _context.SessionTokens.Include(s => s.User).FirstOrDefaultAsync(s => s.Id == sessionToken);
            var user = token?.User;
            if (user == null)
            {
                return Problem("User not found, please login");
            }
            if (post.Poster != user)
            {
                return Problem("You are not authorized to delete this post");
            }

            _context.Posts.Remove(post);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Post deleted" });
        }
    }
}
