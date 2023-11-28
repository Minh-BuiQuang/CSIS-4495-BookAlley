using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using BookAlleyWebApi.Models;
using BookAlleyWebApi.RestModels;

namespace BookAlleyWebApi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ConversationsController : ControllerBase
    {
        private readonly BookAlleyContext _context;

        public ConversationsController(BookAlleyContext context)
        {
            _context = context;
        }

        // GET: api/Conversations
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ConversationResponse>>> GetConversations([FromQuery] Guid sessionToken)
        {
            if (_context.Conversations == null)
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
            //Get conversations where user is either the poster or the finder
            var conversations = await _context.Conversations.Where(c => c.PosterId == user.Id || c.FinderId == user.Id)
                .Include(c => c.Messages)
                .Include(c => c.Finder)
                .Include(c=> c.Poster)
                .ToListAsync();
            var results = new List<ConversationResponse>();
            foreach (var conversation in conversations)
            {
                var result = new ConversationResponse()
                {
                    Id = conversation.Id,
                    FinderId = conversation.FinderId,
                    FinderName = conversation.Finder.Name,
                    PosterId = conversation.PosterId,
                    PosterName = conversation.Poster.Name,
                    Messages = conversation.Messages
                };
                results.Add(result);
            }
            return results;
        }
        [HttpPost]
        public async Task<ActionResult<long>> PostConversation([FromQuery] Guid sessionToken, [FromBody] CreateConversationRequest body)
        {
            if (_context.Conversations == null)
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
            //Get conversation by user Id and poster Id
            var conversation = await _context.Conversations.FirstOrDefaultAsync(c => c.FinderId == user.Id && c.PosterId == body.PosterId);
            //Return conversation Id if conversation exists
            if (conversation != null) return conversation.Id;
            //Create new conversation if conversation does not exist
            conversation = new Conversation()
            {
                FinderId = user.Id,
                PosterId = body.PosterId
            };
            conversation.Messages = new List<Message>();
            _context.Conversations.Add(conversation);
            await _context.SaveChangesAsync();
            //Add the system message to the conversation
            //Get the poster by name
            var poster = await _context.Users.FirstOrDefaultAsync(u => u.Id == body.PosterId);
            //Get the post by post Id
            var post = await _context.Posts.FirstOrDefaultAsync(p => p.Id == body.PostId);
            _context.Messages.Add(new Message()
            {
                Content = user.Name + "started the conversation with " + poster?.Name + " about the book: " + post?.BookTitle,
                Source = Message.MessageSource.system,
                Timestamp = DateTime.Now,
                Conversation = conversation
            });
            await _context.SaveChangesAsync();
            return conversation.Id;
        }
        
    }
}
