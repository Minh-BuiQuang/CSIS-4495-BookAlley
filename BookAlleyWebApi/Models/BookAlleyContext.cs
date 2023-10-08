using Microsoft.EntityFrameworkCore;

namespace BookAlleyWebApi.Models
{
    public class BookAlleyContext : DbContext
    {
        public BookAlleyContext(DbContextOptions<BookAlleyContext> options)
            : base(options)
        {
        }

        public DbSet<User> Users { get; set; }
        public DbSet<Conversation> Conversations { get; set; }
        public DbSet<Message> Messages { get; set; }
        public DbSet<Post> Posts { get; set; }
        public DbSet<SessionToken> SessionTokens { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<Conversation>()
                .HasOne(c => c.Poster)
                .WithMany(u => u.PosterConversations)
                .HasForeignKey(c => c.PosterId)
                .OnDelete(DeleteBehavior.NoAction);

            modelBuilder.Entity<Conversation>()
                .HasOne(c => c.Finder)
                .WithMany(u => u.FinderConversations)
                .HasForeignKey(c => c.FinderId)
                .OnDelete(DeleteBehavior.NoAction);
        }
    }
}
